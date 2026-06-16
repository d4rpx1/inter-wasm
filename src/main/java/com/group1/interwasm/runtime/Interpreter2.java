package com.group1.interwasm.runtime;

import com.group1.interwasm.instruction.arithmetic.*;
import com.group1.interwasm.instruction.comparisons.*;
import com.group1.interwasm.instruction.control_flow.*;
import com.group1.interwasm.instruction.i32.ConstI32;
import com.group1.interwasm.instruction.locals.*;
import com.group1.interwasm.model.FunctionDef;
import com.group1.interwasm.model.Instruction;

import java.util.Arrays;
import java.util.List;

/**
 * Optimized interpreter — identical semantics to Interpreter, faster data structures:
 *
 *   int[] stack + int sp   instead of ArrayDeque<WasmValue>
 *     → array slot read/write (iaload/iastore) instead of 3× invokevirtual per arithmetic op
 *     → no WasmValue record allocated per pushed value
 *
 *   int[] locals            instead of List<WasmValue>
 *     → direct array access instead of invokevirtual List.get() + WasmValue.asI32()
 *
 *   int signal return       instead of ControlSignal sealed interface
 *     → 0 = NEXT, -1 = RETURN, (n+1) = Branch(n)
 *     → no Branch record allocated per br instruction
 *
 *   In-place binary ops:    stack[sp-1] op= stack[sp]; sp--
 *     → one fewer array write vs pop+pop+push
 *
 * Not thread-safe: stack/locals/sp are instance fields reset on each invoke().
 */
public final class Interpreter2 {

    private static final int SIG_NEXT   =  0;
    private static final int SIG_RETURN = -1;
    // Branch(n) encodes as n+1; decode: depth = sig-1

    private final FunctionDef function;
    private final ExecutionStats stats;

    private final int[] stack;
    private int sp;
    private final int[] locals;

    public Interpreter2(FunctionDef functionDef) {
        this(functionDef, null);
    }

    public Interpreter2(FunctionDef functionDef, ExecutionStats stats) {
        this.function = functionDef;
        this.stats    = stats;
        int localCount = functionDef.type().paramCount() + functionDef.locals().size();
        this.locals = new int[localCount];
        this.stack  = new int[256];
    }

    public WasmValue invoke(List<WasmValue> args) {
        if (stats != null) stats.reset();
        sp = -1;
        for (int i = 0; i < args.size(); i++) locals[i] = args.get(i).asI32();
        Arrays.fill(locals, args.size(), locals.length, 0);
        executeBody(function.body());
        return WasmValue.i32(stack[sp--]);
    }

    private int executeBody(List<Instruction> body) {
        for (Instruction instruction : body) {
            int sig = execute(instruction);
            if (sig != SIG_NEXT) return sig;
        }
        return SIG_NEXT;
    }

    private int execute(Instruction instruction) {
        if (stats != null) stats.dispatches++;

        switch (instruction) {
            case ConstI32 c  -> stack[++sp] = c.value();

            // In-place binary ops: modify second-to-top element, decrement sp
            case I32Add  _   -> { stack[sp - 1] += stack[sp]; sp--; }
            case I32Sub  _   -> { stack[sp - 1] -= stack[sp]; sp--; }
            case I32Mul  _   -> { stack[sp - 1] *= stack[sp]; sp--; }
            case I32DivS _   -> {
                int b = stack[sp--];
                if (b == 0) throw new ArithmeticException("integer divide by zero");
                if (stack[sp] == Integer.MIN_VALUE && b == -1) throw new ArithmeticException("integer overflow");
                stack[sp] /= b;
            }
            case I32RemS _   -> {
                int b = stack[sp--];
                if (b == 0) throw new ArithmeticException("integer divide by zero");
                stack[sp] %= b;
            }

            // In-place unary/binary comparisons
            case I32Eqz  _   -> stack[sp]     = (stack[sp]     == 0)         ? 1 : 0;
            case I32Eq   _   -> { stack[sp - 1] = (stack[sp - 1] == stack[sp]) ? 1 : 0; sp--; }
            case I32Ne   _   -> { stack[sp - 1] = (stack[sp - 1] != stack[sp]) ? 1 : 0; sp--; }
            case I32LtS  _   -> { stack[sp - 1] = (stack[sp - 1] <  stack[sp]) ? 1 : 0; sp--; }
            case I32GtS  _   -> { stack[sp - 1] = (stack[sp - 1] >  stack[sp]) ? 1 : 0; sp--; }
            case I32LeS  _   -> { stack[sp - 1] = (stack[sp - 1] <= stack[sp]) ? 1 : 0; sp--; }
            case I32GeS  _   -> { stack[sp - 1] = (stack[sp - 1] >= stack[sp]) ? 1 : 0; sp--; }

            case LocalGet g  -> stack[++sp] = locals[g.index()];
            case LocalSet s  -> locals[s.index()] = stack[sp--];
            case LocalTee t  -> locals[t.index()] = stack[sp]; // peek: sp unchanged

            case Block block -> {
                int savedSp = sp;
                int sig = executeBody(block.body());
                if (sig > 0) {   // Branch: absorb depth=0, propagate depth-1 otherwise
                    sp = savedSp;
                    int depth = sig - 1;
                    return depth == 0 ? SIG_NEXT : depth; // depth re-encodes as Branch(depth-1)
                }
                return sig;
            }

            case Loop loop -> {
                int savedSp = sp;
                while (true) {
                    int sig = executeBody(loop.body());
                    if (sig == SIG_NEXT)   return SIG_NEXT;
                    if (sig == SIG_RETURN) return SIG_RETURN;
                    int depth = sig - 1;
                    if (depth == 0) { sp = savedSp; continue; } // Branch(0) in loop = restart
                    sp = savedSp;
                    return depth; // Branch(depth-1)
                }
            }

            case If ifInstr -> {
                int cond = stack[sp--];
                int savedSp = sp;
                List<Instruction> body = cond != 0 ? ifInstr.thenBody() : ifInstr.elseBody();
                int sig = executeBody(body);
                if (sig > 0) {
                    sp = savedSp;
                    int depth = sig - 1;
                    return depth == 0 ? SIG_NEXT : depth;
                }
                return sig;
            }

            case Br   br   -> { return br.labelIndex() + 1; }
            case BrIf brIf -> { if (stack[sp--] != 0) return brIf.labelIndex() + 1; }
            case Return  _ -> { return SIG_RETURN; }

            case End         _ -> { /* consumed by parser, no-op */ }
            case Nop         _ -> { /* no-op */ }
            case Unreachable _ -> throw new RuntimeException("unreachable");

            default -> throw new UnsupportedOperationException("Unsupported: " + instruction);
        }

        return SIG_NEXT;
    }
}
