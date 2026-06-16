package com.group1.interwasm.runtime;
import com.group1.interwasm.instruction.i32.ConstI32;
import com.group1.interwasm.instruction.arithmetic.I32Add;
import com.group1.interwasm.model.FunctionDef;
import com.group1.interwasm.model.Instruction;

import java.util.List;

/**
 * Interpreter for one single function
 */
public final class Interpreter {
    private final FunctionDef function;

    public Interpreter(FunctionDef functionDef) {
        this.function = functionDef;
    }

    public WasmValue invoke(List<WasmValue> args) {
        Frame frame = new Frame(function, args);

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
        
        OperandStack stack = frame.operandStack();

        switch (instruction) {
            case ConstI32 c -> {
                frame.operandStack().push(WasmValue.i32(c.value()));
            }

            case I32Add ignored -> {
                int b = frame.operandStack().popI32();
                int a = frame.operandStack().popI32();

                frame.operandStack().pushI32(a + b);
            }

            // TODO: add other instructions

            default -> throw new UnsupportedOperationException(
                    "Unsupported instruction: " + instruction
            );
        }
    }
}