package com.group1.interwasm.runtime;

import com.group1.interwasm.instruction.i32.ConstI32;
import com.group1.interwasm.instruction.locals.LocalGet;
import com.group1.interwasm.model.FunctionDef;
import com.group1.interwasm.model.FunctionType;
import com.group1.interwasm.model.Instruction;
import com.group1.interwasm.model.ValueType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest extends InterpreterContractTest {

    @Override
    int run(List<Instruction> body) {
        return new Interpreter(fn(body)).invoke(List.of()).asI32();
    }

<<<<<<< Updated upstream
    // Arithmetic 

    @Test
    void addsTwoI32Values() {
        assertEquals(5, run(List.of(new ConstI32(2), new ConstI32(3), new I32Add())));
        assertEquals(0, run(List.of(new ConstI32(0), new ConstI32(0), new I32Add())));
        assertEquals(-2, run(List.of(new ConstI32(-1), new ConstI32(-1), new I32Add())));
        assertEquals(0, run(List.of(new ConstI32(1), new ConstI32(-1), new I32Add())));
        assertEquals(Integer.MIN_VALUE, run(List.of(new ConstI32(Integer.MAX_VALUE), new ConstI32(1), new I32Add())));
=======
    @Override
    WasmValue invoke(FunctionDef fn, List<WasmValue> args) {
        return new Interpreter(fn).invoke(args);
>>>>>>> Stashed changes
    }

    // ── Frame-specific validation (not part of the shared contract) ───────────

<<<<<<< Updated upstream
    @Test
    void multipliesI32Values() {
        assertEquals(1, run(List.of(new ConstI32(-1), new ConstI32(-1), new I32Mul())));
        assertEquals(2, run(List.of(new ConstI32(-2), new ConstI32(-1), new I32Mul())));
        assertEquals(-2, run(List.of(new ConstI32(-2), new ConstI32(1), new I32Mul())));
        assertEquals(-2, run(List.of(new ConstI32(2), new ConstI32(-1), new I32Mul())));
        assertEquals(4, run(List.of(new ConstI32(-2), new ConstI32(-2), new I32Mul())));
        assertEquals(1, run(List.of(new ConstI32(1), new ConstI32(1), new I32Mul())));
        assertEquals(2, run(List.of(new ConstI32(2), new ConstI32(1), new I32Mul())));
        assertEquals(20, run(List.of(new ConstI32(4), new ConstI32(5), new I32Mul())));
    }

    @Test
    void dividesSigned() {
        assertEquals(3, run(List.of(new ConstI32(10), new ConstI32(3), new I32DivS())));
        assertEquals(0, run(List.of(new ConstI32(1), new ConstI32(3), new I32DivS())));
        assertEquals(1, run(List.of(new ConstI32(4), new ConstI32(4), new I32DivS())));
    }

    @Test
    void divisionTruncatesTowardZero() {
        assertEquals(-3, run(List.of(new ConstI32(-10), new ConstI32(3), new I32DivS())));
    }

    @Test
    void divisionTruncatesTowardZeroForAllSignCombinations() {
        assertEquals(-3, run(List.of(new ConstI32(-10), new ConstI32(3), new I32DivS())));
        assertEquals(-3, run(List.of(new ConstI32(10), new ConstI32(-3), new I32DivS())));
        assertEquals(3, run(List.of(new ConstI32(-10), new ConstI32(-3), new I32DivS())));
    }

    @Test
    void remainderSigned() {
        assertEquals(1, run(List.of(new ConstI32(10), new ConstI32(3), new I32RemS())));
    }

    @Test
    void divideByZeroTraps() {
        assertThrows(ArithmeticException.class, () ->
                run(List.of(new ConstI32(5), new ConstI32(0), new I32DivS())));
        assertThrows(ArithmeticException.class, () ->
                run(List.of(new ConstI32(0), new ConstI32(0), new I32DivS())));
    }

    @Test
    void divideMinValueByNegOneTraps() {
        assertThrows(ArithmeticException.class, () ->
                run(List.of(new ConstI32(Integer.MIN_VALUE), new ConstI32(-1), new I32DivS())));
    }

    @Test
    void remainderByZeroTraps() {
        assertThrows(ArithmeticException.class, () ->
                run(List.of(new ConstI32(5), new ConstI32(0), new I32RemS())));
    }

    @Test
    void remainderKeepsSignOfDividend() {
        assertEquals(1, run(List.of(new ConstI32(10), new ConstI32(3), new I32RemS())));
        assertEquals(-1, run(List.of(new ConstI32(-10), new ConstI32(3), new I32RemS())));
        assertEquals(1, run(List.of(new ConstI32(10), new ConstI32(-3), new I32RemS())));
        assertEquals(-1, run(List.of(new ConstI32(-10), new ConstI32(-3), new I32RemS())));
    }

    @Test
    void multiplicationWrapsModulo32Bit() {
        assertEquals(0, run(List.of(
                        new ConstI32(65536),
                        new ConstI32(65536),
                        new I32Mul()
                        )));

        assertEquals(-2, run(List.of(
                        new ConstI32(Integer.MAX_VALUE),
                        new ConstI32(2),
                        new I32Mul()
                        )));

        assertEquals(Integer.MIN_VALUE, run(List.of(
                        new ConstI32(Integer.MIN_VALUE),
                        new ConstI32(-1),
                        new I32Mul()
                        )));
    }

    //  Comparisons 

    @Test
    void eqzReturnsTrueForZero() {
        assertEquals(1, run(List.of(new ConstI32(0), new I32Eqz())));
    }

    @Test
    void eqzReturnsFalseForNonZero() {
        assertEquals(0, run(List.of(new ConstI32(42), new I32Eqz())));
    }

    @Test
    void eqReturnOneForEqualValues() {
        assertEquals(1, run(List.of(new ConstI32(5), new ConstI32(5), new I32Eq())));
    }

    @Test
    void eqReturnZeroForDifferentValues() {
        assertEquals(0, run(List.of(new ConstI32(5), new ConstI32(6), new I32Eq())));
    }

    @Test
    void neReturnOneForDifferentValues() {
        assertEquals(1, run(List.of(new ConstI32(5), new ConstI32(6), new I32Ne())));
    }

    @Test
    void neReturnZeroForEqualValues() {
        assertEquals(0, run(List.of(new ConstI32(5), new ConstI32(5), new I32Ne())));
    }

    @Test
    void ltSReturnOneWhenLess() {
        assertEquals(1, run(List.of(new ConstI32(3), new ConstI32(5), new I32LtS())));
    }

    @Test
    void ltSReturnZeroWhenNotLess() {
        assertEquals(0, run(List.of(new ConstI32(5), new ConstI32(3), new I32LtS())));
    }

    @Test
    void gtSReturnOneWhenGreater() {
        assertEquals(1, run(List.of(new ConstI32(5), new ConstI32(3), new I32GtS())));
    }

    @Test
    void gtSReturnZeroWhenNotGreater() {
        assertEquals(0, run(List.of(new ConstI32(3), new ConstI32(5), new I32GtS())));
    }

    @Test
    void leSReturnOneWhenLessOrEqual() {
        assertEquals(1, run(List.of(new ConstI32(3), new ConstI32(3), new I32LeS())));
        assertEquals(1, run(List.of(new ConstI32(2), new ConstI32(3), new I32LeS())));
    }

    @Test
    void geSReturnOneWhenGreaterOrEqual() {
        assertEquals(1, run(List.of(new ConstI32(3), new ConstI32(3), new I32GeS())));
        assertEquals(1, run(List.of(new ConstI32(4), new ConstI32(3), new I32GeS())));
    }

    //  Locals 
    

    @Test
    void invokeRejectsWrongArgumentCount() {
=======
    @Test void invokeRejectsWrongArgumentCount() {
>>>>>>> Stashed changes
        assertThrows(IllegalArgumentException.class, () ->
                new Interpreter(fn1(List.of(new LocalGet(0)))).invoke(List.of()));
    }

    @Test void localGetRejectsInvalidIndex() {
        assertThrows(IllegalArgumentException.class, () ->
                run(List.of(new LocalGet(99))));
    }

    @Test void extraLocalInitializedToZeroViaFrame() {
        FunctionDef fn = new FunctionDef(
                new FunctionType(List.of(), ValueType.I32),
                List.of(ValueType.I32),
                List.of(new LocalGet(0)));
        assertEquals(0, new Interpreter(fn).invoke(List.of()).asI32());
    }
<<<<<<< Updated upstream

    @Test
    void localSetAndGet() {
        // param=local 0, extra local=local 1
        FunctionDef fn = new FunctionDef(
                new FunctionType(List.of(ValueType.I32), ValueType.I32),
                List.of(ValueType.I32),
                List.of(new ConstI32(99), new LocalSet(1), new LocalGet(1)));
        assertEquals(99, new Interpreter(fn).invoke(List.of(WasmValue.i32(0))).asI32());
    }

    @Test
    void localTeeSetAndLeaveOnStack() {
        // Push 77, tee local 0 (sets it, leaves 77 on stack), add 1 → 78
        FunctionDef fn = new FunctionDef(
                new FunctionType(List.of(), ValueType.I32),
                List.of(ValueType.I32),
                List.of(new ConstI32(77), new LocalTee(0), new ConstI32(1), new I32Add()));
        assertEquals(78, new Interpreter(fn).invoke(List.of()).asI32());
    }

    //  Control flow: block 

    @Test
    void blockRunsToCompletion() {
        assertEquals(42, run(List.of(new Block(List.of(new ConstI32(42))))));
    }

    @Test
    void brExitsBlock() {
        // block { br 0 }; const 42 → 42 (const 99 inside block never pushed)
        assertEquals(42, run(List.of(
                new Block(List.of(new Br(0))),
                new ConstI32(42))));
    }

    @Test
    void brIfExitsBlockWhenConditionTrue() {
        // block { const 1; br_if 0 }; const 42 → 42
        assertEquals(42, run(List.of(
                new Block(List.of(new ConstI32(1), new BrIf(0))),
                new ConstI32(42))));
    }

    @Test
    void brIfContinuesWhenConditionFalse() {
        // block { const 0; br_if 0; const 42 } → 42 (falls through)
        assertEquals(42, run(List.of(
                new Block(List.of(new ConstI32(0), new BrIf(0), new ConstI32(42))))));
    }

    @Test
    void nestedBlocksBrTargetsOuter() {
        // outer block { inner block { br 1 }; const 99 (skipped) }; const 42
        // br 1: depth 0 = inner, depth 1 = outer → exits outer block
        assertEquals(42, run(List.of(
                new Block(List.of(
                        new Block(List.of(new Br(1))),
                        new ConstI32(99)    // never reached
                )),
                new ConstI32(42))));
    }

    @Test
    void brIfTrueUnwindsStackToBlockEntryHeight() {
        assertEquals(8, run(List.of(
                        new ConstI32(7),
                        new Block(List.of(
                                new ConstI32(99),
                                new ConstI32(1),
                                new BrIf(0)
                                )),
                        new ConstI32(1),
                        new I32Add()
                        )));
    }

    @Test
    void brUnwindsStackToBlockEntryHeight() {
        // 7 bleibt vor dem block auf dem Stack.
        // 99 wird im block gepusht, muss durch br 0 aber verworfen werden.
        // Danach: 7 + 1 = 8
        assertEquals(8, run(List.of(
                        new ConstI32(7),
                        new Block(List.of(
                                new ConstI32(99),
                                new Br(0)
                                )),
                        new ConstI32(1),
                        new I32Add()
                        )));
    }

    //  Control flow: loop 

    @Test
    void emptyLoopBodyExitsNormally() {
        // const 42; loop {} → 42 still on stack
        assertEquals(42, run(List.of(new ConstI32(42), new Loop(List.of()))));
    }

    @Test
    void loopSumsZeroToFour() {
        // Locals: 0=sum, 1=i (all extra, initially 0)
        // block { loop { i>=5 → br_if 1 (exit); sum+=i; i+=1; br 0 (restart) } }
        // Result: 0+1+2+3+4 = 10
        FunctionDef fn = new FunctionDef(
                new FunctionType(List.of(), ValueType.I32),
                List.of(ValueType.I32, ValueType.I32),
                List.of(
                        new Block(List.of(
                                new Loop(List.of(
                                        new LocalGet(1), new ConstI32(5), new I32GeS(), new BrIf(1),
                                        new LocalGet(0), new LocalGet(1), new I32Add(), new LocalSet(0),
                                        new LocalGet(1), new ConstI32(1), new I32Add(), new LocalSet(1),
                                        new Br(0)
                                ))
                        )),
                        new LocalGet(0)
                ));
        assertEquals(10, new Interpreter(fn).invoke(List.of()).asI32());
    }

    //  Control flow: if 

    @Test
    void ifRunsThenBodyWhenTrue() {
        // const 1 (true); if { const 42 } else { const 0 } → 42
        assertEquals(42, run(List.of(
                new ConstI32(1),
                new If(List.of(new ConstI32(42)), List.of(new ConstI32(0))))));
    }

    @Test
    void ifRunsElseBodyWhenFalse() {
        // const 0 (false); if { const 42 } else { const 99 } → 99
        assertEquals(99, run(List.of(
                new ConstI32(0),
                new If(List.of(new ConstI32(42)), List.of(new ConstI32(99))))));
    }

    @Test
    void ifWithEmptyElseAndFalseConditionDoesNothing() {
        // const 42; const 0; if { const 99 } (no else) → 42 (on stack from before)
        assertEquals(42, run(List.of(
                new ConstI32(42),
                new ConstI32(0),
                new If(List.of(new ConstI32(99)), List.of()))));
    }

    @Test
    void brInsideIfExitsIf() {
        // const 1 (true); if { br 0; const 99 (unreachable) } else {}; const 42
        assertEquals(42, run(List.of(
                new ConstI32(1),
                new If(List.of(new Br(0), new ConstI32(99)), List.of()),
                new ConstI32(42))));
    }

    //  Control flow: return 

    @Test
    void returnExitsEarlyWithValueOnStack() {
        // const 42; return; const 99 (never reached) → 42
        assertEquals(42, run(List.of(new ConstI32(42), new Return(), new ConstI32(99))));
    }

    @Test
    void returnInsideBlockUnwindsAll() {
        // block { const 42; return; const 99 }; const 0 (never reached) → 42
        assertEquals(42, run(List.of(
                new Block(List.of(new ConstI32(42), new Return(), new ConstI32(99))),
                new ConstI32(0))));
    }

    @Test
    void returnInsideLoopUnwindsAll() {
        // block { loop { const 42; return } }; const 0 (never reached) → 42
        assertEquals(42, run(List.of(
                new Block(List.of(
                        new Loop(List.of(new ConstI32(42), new Return()))
                )),
                new ConstI32(0))));
    }

    //  Nop / Unreachable 

    @Test
    void nopDoesNothing() {
        assertEquals(42, run(List.of(new Nop(), new ConstI32(42), new Nop())));
    }

    @Test
    void unreachableTraps() {
        assertThrows(RuntimeException.class, () -> run(List.of(new Unreachable())));
    }

    @Test
    void unreachableIsNotReachedWhenBranchSkipsIt() {
        // block { br 0; unreachable (skipped) }; const 42
        assertEquals(42, run(List.of(
                new Block(List.of(new Br(0), new Unreachable())),
                new ConstI32(42))));
    }
=======
>>>>>>> Stashed changes
}
