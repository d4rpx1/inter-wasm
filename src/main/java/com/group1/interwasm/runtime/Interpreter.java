package com.group1.interwasm.runtime;

import com.group1.interwasm.instruction.arithmetic.*;
import com.group1.interwasm.instruction.comparisons.*;
import com.group1.interwasm.instruction.control_flow.*;
import com.group1.interwasm.instruction.i32.ConstI32;
import com.group1.interwasm.instruction.locals.*;
import com.group1.interwasm.model.FunctionDef;
import com.group1.interwasm.model.Instruction;
import com.group1.interwasm.util.OperandStack;

import java.util.List;

public final class Interpreter {
    private final FunctionDef function;
    private final ExecutionStats stats;

    public Interpreter(FunctionDef functionDef) {
        this(functionDef, null);
    }

    public Interpreter(FunctionDef functionDef, ExecutionStats stats) {
        this.function = functionDef;
        this.stats = stats;
    }

    public WasmValue invoke(List<WasmValue> args) {
        if (stats != null) stats.reset();
        Frame frame = new Frame(function, args, stats);
        executeBody(function.body(), frame);
        return frame.operandStack().pop();
    }

    private ControlSignal executeBody(List<Instruction> body, Frame frame) {
        for (Instruction instruction : body) {
            ControlSignal signal = execute(instruction, frame);
            if (!(signal instanceof ControlSignal.Next)) {
                return signal;
            }
        }
        return ControlSignal.NEXT;
    }

    private ControlSignal execute(Instruction instruction, Frame frame) {
        if (stats != null) stats.dispatches++;
        OperandStack stack = frame.operandStack();

        switch (instruction) {
            case ConstI32 c -> stack.pushI32(c.value());

            case I32Add _ -> {
                int b = stack.popI32();
                int a = stack.popI32();
                stack.pushI32(a + b);
            }
            case I32Sub _ -> {
                int b = stack.popI32();
                int a = stack.popI32();
                stack.pushI32(a - b);
            }
            case I32Mul _ -> {
                int b = stack.popI32();
                int a = stack.popI32();
                stack.pushI32(a * b);
            }
            case I32DivS _ -> {
                int b = stack.popI32();
                int a = stack.popI32();
                if (b == 0) throw new ArithmeticException("integer divide by zero");
                if (a == Integer.MIN_VALUE && b == -1) throw new ArithmeticException("integer overflow");
                stack.pushI32(a / b);
            }
            case I32RemS _ -> {
                int b = stack.popI32();
                int a = stack.popI32();
                if (b == 0) throw new ArithmeticException("integer divide by zero");
                stack.pushI32(a % b);
            }

            case I32Eqz _ -> stack.pushI32(stack.popI32() == 0 ? 1 : 0);
            case  I32Eq   _  ->  {  int  b  =  stack.popI32();  stack.pushI32(stack.popI32()  ==  b  ?  1  :  0);  }
            case  I32Ne   _  ->  {  int  b  =  stack.popI32();  stack.pushI32(stack.popI32()  !=  b  ?  1  :  0);  }
            case  I32LtS  _  ->  {  int  b  =  stack.popI32();  stack.pushI32(stack.popI32()  <   b  ?  1  :  0);  }
            case  I32GtS  _  ->  {  int  b  =  stack.popI32();  stack.pushI32(stack.popI32()  >   b  ?  1  :  0);  }
            case  I32LeS  _  ->  {  int  b  =  stack.popI32();  stack.pushI32(stack.popI32()  <=  b  ?  1  :  0);  }
            case  I32GeS  _  ->  {  int  b  =  stack.popI32();  stack.pushI32(stack.popI32()  >=  b  ?  1  :  0);  }

            case LocalGet g -> stack.push(frame.getLocal(g.index()));
            case LocalSet s -> frame.setLocal(s.index(), stack.pop());
            case LocalTee t -> frame.setLocal(t.index(), stack.peek());

            case Block block -> {
                int savedDepth = stack.size();
                ControlSignal signal = executeBody(block.body(), frame);
                if (signal instanceof ControlSignal.Branch b && b.depth() == 0) {
                    stack.truncateTo(savedDepth);
                    return ControlSignal.NEXT;
                } else if (signal instanceof ControlSignal.Branch b) {
                    stack.truncateTo(savedDepth);
                    return new ControlSignal.Branch(b.depth() - 1);
                }
                return signal;
            }

            case Loop loop -> {
                int savedDepth = stack.size();
                while (true) {
                    ControlSignal signal = executeBody(loop.body(), frame);
                    if (signal instanceof ControlSignal.Next) {
                        return ControlSignal.NEXT;
                    } else if (signal instanceof ControlSignal.Branch b && b.depth() == 0) {
                        stack.truncateTo(savedDepth);
                        // Branch(0) in a loop = restart (backward jump)
                    } else if (signal instanceof ControlSignal.Branch b) {
                        stack.truncateTo(savedDepth);
                        return new ControlSignal.Branch(b.depth() - 1);
                    } else {
                        return signal; // FunctionReturn
                    }
                }
            }

            case If ifInstr -> {
                int cond = stack.popI32();
                int savedDepth = stack.size();
                List<Instruction> body = cond != 0 ? ifInstr.thenBody() : ifInstr.elseBody();
                ControlSignal signal = executeBody(body, frame);
                if (signal instanceof ControlSignal.Branch b && b.depth() == 0) {
                    stack.truncateTo(savedDepth);
                    return ControlSignal.NEXT;
                } else if (signal instanceof ControlSignal.Branch b) {
                    stack.truncateTo(savedDepth);
                    return new ControlSignal.Branch(b.depth() - 1);
                }
                return signal;
            }

            case Br br -> { return new ControlSignal.Branch(br.labelIndex()); }
            case BrIf brIf -> {
                int cond = stack.popI32();
                if (cond != 0) return new ControlSignal.Branch(brIf.labelIndex());
            }
            case Return _ -> { return ControlSignal.RETURN; }

            case End _ -> { /* consumed by parser, no-op */ }
            case Nop _ -> { /* no-op */ }
            case Unreachable _ -> throw new RuntimeException("unreachable");

            default -> throw new UnsupportedOperationException("Unsupported instruction: " + instruction);
        }

        return ControlSignal.NEXT;
    }
}
