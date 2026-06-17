package com.group1.interwasm.runtime;

import com.group1.interwasm.instruction.arithmetic.*;
import com.group1.interwasm.instruction.comparisons.*;
import com.group1.interwasm.instruction.control_flow.*;
import com.group1.interwasm.instruction.i32.ConstI32;
import com.group1.interwasm.instruction.locals.*;
import com.group1.interwasm.model.FunctionDef;
import com.group1.interwasm.model.Instruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Flat-bytecode interpreter (v3).
 *
 * Compilation (one-time, done in constructor):
 *   List<Instruction> tree -> int[] with all branch targets resolved (backpatched).
 *
 * Dispatch (every invoke):
 *   Single while-loop + int switch over the flat array.
 *   No recursion, no Iterator, no ControlSignal allocation, no object dispatch.
 *
 * Opcode layout (each slot is one int):
 *   0-arg : OP_I32_ADD … OP_UNREACHABLE
 *   1-arg : OP_I32_CONST value | OP_LOCAL_GET idx | OP_LOCAL_SET idx | OP_LOCAL_TEE idx
 *           OP_JUMP target | OP_JUMP_IF_Z target
 *   2-arg : OP_JUMP_SP target savedSp | OP_JUMP_IF_SP target savedSp
 *
 * Jump semantics:
 *   OP_JUMP       target            -> pc = target
 *   OP_JUMP_IF_Z  target            -> pop cond; if cond==0: pc = target
 *   OP_JUMP_SP    target savedSp    -> pc = target; sp = savedSp   (br/br_if backward to loop)
 *   OP_JUMP_IF_SP target savedSp    -> pop cond; if cond!=0: pc = target; sp = savedSp
 *
 * The savedSp in JUMP_SP / JUMP_IF_SP equals the runtime sp at the time the enclosing
 * block/loop/if was entered. It resets any garbage pushed inside the construct before
 * the branch fires (mirrors the sp = savedSp resets in Interpreter2).
 */
public final class Interpreter3 {

    //  Opcodes 
    private static final int OP_I32_ADD     =  0;
    private static final int OP_I32_SUB     =  1;
    private static final int OP_I32_MUL     =  2;
    private static final int OP_I32_DIV_S   =  3;
    private static final int OP_I32_REM_S   =  4;
    private static final int OP_I32_EQZ     =  5;
    private static final int OP_I32_EQ      =  6;
    private static final int OP_I32_NE      =  7;
    private static final int OP_I32_LT_S    =  8;
    private static final int OP_I32_GT_S    =  9;
    private static final int OP_I32_LE_S    = 10;
    private static final int OP_I32_GE_S    = 11;
    private static final int OP_RETURN      = 12;
    private static final int OP_UNREACHABLE = 13;
    // 1-arg
    private static final int OP_I32_CONST   = 14;
    private static final int OP_LOCAL_GET   = 15;
    private static final int OP_LOCAL_SET   = 16;
    private static final int OP_LOCAL_TEE   = 17;
    private static final int OP_JUMP        = 18;   // unconditional; next slot = target
    private static final int OP_JUMP_IF_Z   = 19;   // pop + jump-if-zero; next slot = target
    // 2-arg
    private static final int OP_JUMP_SP     = 20;   // unconditional + sp reset; next 2 = target, savedSp
    private static final int OP_JUMP_IF_SP  = 21;   // pop + jump-if-nonzero + sp reset; next 2 = target, savedSp

    //  State 
    private final int[] code;
    private final int[] stack = new int[256];
    private final int[] locals;
    private int sp;

    public Interpreter3(FunctionDef fn) {
        int localCount = fn.type().paramCount() + fn.locals().size();
        this.locals = new int[localCount];
        this.code   = new Compiler().compile(fn.body());
    }

    public WasmValue invoke(List<WasmValue> args) {
        sp = -1;
        for (int i = 0; i < args.size(); i++) locals[i] = args.get(i).asI32();
        Arrays.fill(locals, args.size(), locals.length, 0);
        run();
        return WasmValue.i32(stack[sp--]);
    }

    //  Dispatch loop 

    private void run() {
        final int[] c = code;
        final int[] s = stack;
        final int[] l = locals;
        int pc = 0;
        int sp = this.sp;

        loop:
        while (pc < c.length) {
            switch (c[pc++]) {
                case OP_I32_ADD    -> { s[sp - 1] += s[sp]; sp--; }
                case OP_I32_SUB    -> { s[sp - 1] -= s[sp]; sp--; }
                case OP_I32_MUL    -> { s[sp - 1] *= s[sp]; sp--; }
                case OP_I32_DIV_S  -> {
                    int b = s[sp--];
                    if (b == 0) throw new ArithmeticException("integer divide by zero");
                    if (s[sp] == Integer.MIN_VALUE && b == -1) throw new ArithmeticException("integer overflow");
                    s[sp] /= b;
                }
                case OP_I32_REM_S  -> {
                    int b = s[sp--];
                    if (b == 0) throw new ArithmeticException("integer divide by zero");
                    s[sp] %= b;
                }
                case OP_I32_EQZ    ->   s[sp]     = (s[sp]     == 0)        ? 1 : 0;
                case OP_I32_EQ     -> { s[sp - 1] = (s[sp - 1] == s[sp])    ? 1 : 0; sp--; }
                case OP_I32_NE     -> { s[sp - 1] = (s[sp - 1] != s[sp])    ? 1 : 0; sp--; }
                case OP_I32_LT_S   -> { s[sp - 1] = (s[sp - 1] <  s[sp])    ? 1 : 0; sp--; }
                case OP_I32_GT_S   -> { s[sp - 1] = (s[sp - 1] >  s[sp])    ? 1 : 0; sp--; }
                case OP_I32_LE_S   -> { s[sp - 1] = (s[sp - 1] <= s[sp])    ? 1 : 0; sp--; }
                case OP_I32_GE_S   -> { s[sp - 1] = (s[sp - 1] >= s[sp])    ? 1 : 0; sp--; }
                case OP_RETURN     -> { break loop; }
                case OP_UNREACHABLE -> throw new RuntimeException("unreachable");
                case OP_I32_CONST  ->   s[++sp] = c[pc++];
                case OP_LOCAL_GET  ->   s[++sp] = l[c[pc++]];
                case OP_LOCAL_SET  ->   l[c[pc++]] = s[sp--];
                case OP_LOCAL_TEE  ->   l[c[pc++]] = s[sp];       // peek: sp unchanged
                case OP_JUMP       ->   pc = c[pc];
                case OP_JUMP_IF_Z  -> { int t = c[pc++]; if (s[sp--] == 0) pc = t; }
                case OP_JUMP_SP    -> { int t = c[pc]; sp = c[pc + 1]; pc = t; }
                case OP_JUMP_IF_SP -> { int t = c[pc++]; int ss = c[pc++]; if (s[sp--] != 0) { pc = t; sp = ss; } }
                default -> throw new UnsupportedOperationException("Unknown opcode: " + c[pc - 1]);
            }
        }

        this.sp = sp;
    }

    //  Compiler 

    private static final class Compiler {
        private final List<Integer> buf = new ArrayList<>();
        private final List<Label>   labelStack = new ArrayList<>();

        /**
         * Compile-time stack depth. Mirrors runtime sp: starts at -1 (empty),
         * increments on push, decrements on pop. Stored in Label.savedSp so the
         * executor knows the sp to restore when a branch fires.
         */
        private int stackDepth = -1;

        int[] compile(List<Instruction> body) {
            compileBody(body);
            int[] arr = new int[buf.size()];
            for (int i = 0; i < arr.length; i++) arr[i] = buf.get(i);
            return arr;
        }

        private void compileBody(List<Instruction> body) {
            for (Instruction instr : body) compileOne(instr);
        }

        private void emit(int v)     { buf.add(v); }
        private int  pos()           { return buf.size(); }
        private void patch(int pos, int val) { buf.set(pos, val); }

        private Label labelAt(int depth) {
            return labelStack.get(labelStack.size() - 1 - depth);
        }

        private void compileOne(Instruction instr) {
            switch (instr) {
                case ConstI32  c -> { emit(OP_I32_CONST); emit(c.value()); stackDepth++; }

                case I32Add  _ -> { emit(OP_I32_ADD);   stackDepth--; }
                case I32Sub  _ -> { emit(OP_I32_SUB);   stackDepth--; }
                case I32Mul  _ -> { emit(OP_I32_MUL);   stackDepth--; }
                case I32DivS _ -> { emit(OP_I32_DIV_S); stackDepth--; }
                case I32RemS _ -> { emit(OP_I32_REM_S); stackDepth--; }

                case I32Eqz _ ->   emit(OP_I32_EQZ);
                case I32Eq  _ -> { emit(OP_I32_EQ);   stackDepth--; }
                case I32Ne  _ -> { emit(OP_I32_NE);   stackDepth--; }
                case I32LtS _ -> { emit(OP_I32_LT_S); stackDepth--; }
                case I32GtS _ -> { emit(OP_I32_GT_S); stackDepth--; }
                case I32LeS _ -> { emit(OP_I32_LE_S); stackDepth--; }
                case I32GeS _ -> { emit(OP_I32_GE_S); stackDepth--; }

                case LocalGet g -> { emit(OP_LOCAL_GET); emit(g.index()); stackDepth++; }
                case LocalSet s -> { emit(OP_LOCAL_SET); emit(s.index()); stackDepth--; }
                case LocalTee t -> { emit(OP_LOCAL_TEE); emit(t.index()); }

                //  Control flow 

                case Block block -> {
                    int savedSp = stackDepth;
                    Label label = new Label(Label.Type.BLOCK, savedSp, -1);
                    labelStack.add(label);
                    compileBody(block.body());
                    labelStack.remove(labelStack.size() - 1);
                    // Patch all forward jumps that targeted this block to land here.
                    label.patchAll(buf, pos());
                    // stackDepth is left as-is from the body's fall-through path.
                }

                case Loop loop -> {
                    int savedSp  = stackDepth;
                    int loopStart = pos();
                    Label label = new Label(Label.Type.LOOP, savedSp, loopStart);
                    labelStack.add(label);
                    compileBody(loop.body());
                    labelStack.remove(labelStack.size() - 1);
                    // Patch any forward exits (br/br_if targeting depth > 0) to here.
                    label.patchAll(buf, pos());
                    // Loop is always void: fall-through restores the entry depth.
                    stackDepth = savedSp;
                }

                case If ifInstr -> {
                    stackDepth--;           // pop condition
                    int savedSp = stackDepth;

                    // Jump past then-body if condition is false.
                    emit(OP_JUMP_IF_Z);
                    int condJump = pos();
                    emit(-1);               // patched to else-start or end-of-if

                    Label label = new Label(Label.Type.IF, savedSp, -1);
                    labelStack.add(label);

                    compileBody(ifInstr.thenBody());
                    int afterThenDepth = stackDepth;

                    if (!ifInstr.elseBody().isEmpty()) {
                        // Skip else body after then completes normally.
                        emit(OP_JUMP);
                        int endJump = pos();
                        emit(-1);           // patched to end-of-if

                        patch(condJump, pos());   // else starts here
                        stackDepth = savedSp;
                        compileBody(ifInstr.elseBody());

                        patch(endJump, pos());     // end-of-if
                        stackDepth = afterThenDepth;
                    } else {
                        patch(condJump, pos());   // no else: cond-jump lands here
                        stackDepth = savedSp;     // void if
                    }

                    labelStack.remove(labelStack.size() - 1);
                    // Patch any explicit br(0) inside the if-body to land after the if.
                    label.patchAll(buf, pos());
                }

                case Br br -> {
                    Label target = labelAt(br.labelIndex());
                    if (target.type == Label.Type.LOOP) {
                        // Backward jump: target address is already known.
                        emit(OP_JUMP_SP); emit(target.loopStart); emit(target.savedSp);
                    } else {
                        // Forward jump: patch the target address later.
                        emit(OP_JUMP_SP);
                        target.addPatch(pos()); emit(-1);
                        emit(target.savedSp);
                    }
                    // Code after an unconditional branch is unreachable; stackDepth is a don't-care.
                }

                case BrIf brIf -> {
                    stackDepth--;   // pop condition (consumed regardless of branch direction)
                    Label target = labelAt(brIf.labelIndex());
                    if (target.type == Label.Type.LOOP) {
                        emit(OP_JUMP_IF_SP); emit(target.loopStart); emit(target.savedSp);
                    } else {
                        emit(OP_JUMP_IF_SP);
                        target.addPatch(pos()); emit(-1);
                        emit(target.savedSp);
                    }
                }

                case Return      _ -> emit(OP_RETURN);
                case End         _ -> { /* consumed by parser; no-op */ }
                case Nop         _ -> { /* no-op */ }
                case Unreachable _ -> emit(OP_UNREACHABLE);

                default -> throw new UnsupportedOperationException("Unsupported: " + instr);
            }
        }
    }

    //  Label 

    private static final class Label {
        enum Type { BLOCK, LOOP, IF }

        final Type type;
        final int  savedSp;     // runtime sp at the moment this construct was entered
        final int  loopStart;   // only valid for LOOP: pc of the first loop instruction
        private final List<Integer> patchSites = new ArrayList<>();

        Label(Type type, int savedSp, int loopStart) {
            this.type      = type;
            this.savedSp   = savedSp;
            this.loopStart = loopStart;
        }

        void addPatch(int pos) { patchSites.add(pos); }

        void patchAll(List<Integer> buf, int targetPc) {
            for (int pos : patchSites) buf.set(pos, targetPc);
        }
    }
}
