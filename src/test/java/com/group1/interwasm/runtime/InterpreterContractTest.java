package com.group1.interwasm.runtime;

import com.group1.interwasm.instruction.arithmetic.*;
import com.group1.interwasm.instruction.comparisons.*;
import com.group1.interwasm.instruction.control_flow.*;
import com.group1.interwasm.instruction.i32.ConstI32;
import com.group1.interwasm.instruction.locals.*;
import com.group1.interwasm.model.FunctionDef;
import com.group1.interwasm.model.FunctionType;
import com.group1.interwasm.model.Instruction;
import com.group1.interwasm.model.ValueType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Shared contract tests for all interpreter implementations.
 * Subclasses supply invoke() and the test runner verifies identical semantics.
 */
abstract class InterpreterContractTest {

    /** Execute body in a no-param, no-local, i32-returning function. */
    abstract int run(List<Instruction> body);

    /** Invoke fn with the given args. */
    abstract WasmValue invoke(FunctionDef fn, List<WasmValue> args);

    static FunctionDef fn(List<Instruction> body) {
        return new FunctionDef(
                new FunctionType(List.of(), ValueType.I32),
                List.of(),
                body);
    }

    static FunctionDef fn1(List<Instruction> body) {
        return new FunctionDef(
                new FunctionType(List.of(ValueType.I32), ValueType.I32),
                List.of(),
                body);
    }

    // ── Arithmetic ────────────────────────────────────────────────────────────

    @Test void addsTwoI32Values() {
        assertEquals(5,                 run(List.of(new ConstI32(2), new ConstI32(3), new I32Add())));
        assertEquals(0,                 run(List.of(new ConstI32(0), new ConstI32(0), new I32Add())));
        assertEquals(-2,                run(List.of(new ConstI32(-1), new ConstI32(-1), new I32Add())));
        assertEquals(0,                 run(List.of(new ConstI32(1), new ConstI32(-1), new I32Add())));
        assertEquals(Integer.MIN_VALUE, run(List.of(new ConstI32(Integer.MAX_VALUE), new ConstI32(1), new I32Add())));
    }

    @Test void subtractsI32Values() {
        assertEquals(7,                run(List.of(new ConstI32(10),                new ConstI32(3),  new I32Sub())));
        assertEquals(-3,               run(List.of(new ConstI32(0),                 new ConstI32(3),  new I32Sub())));
        assertEquals(0,                run(List.of(new ConstI32(10),                new ConstI32(10), new I32Sub())));
        assertEquals(-1,               run(List.of(new ConstI32(0),                 new ConstI32(1),  new I32Sub())));
        assertEquals(Integer.MAX_VALUE,run(List.of(new ConstI32(Integer.MIN_VALUE), new ConstI32(1),  new I32Sub())));
    }

    @Test void multipliesI32Values() {
        assertEquals(1,  run(List.of(new ConstI32(-1), new ConstI32(-1), new I32Mul())));
        assertEquals(2,  run(List.of(new ConstI32(-2), new ConstI32(-1), new I32Mul())));
        assertEquals(-2, run(List.of(new ConstI32(-2), new ConstI32(1),  new I32Mul())));
        assertEquals(-2, run(List.of(new ConstI32(2),  new ConstI32(-1), new I32Mul())));
        assertEquals(4,  run(List.of(new ConstI32(-2), new ConstI32(-2), new I32Mul())));
        assertEquals(1,  run(List.of(new ConstI32(1),  new ConstI32(1),  new I32Mul())));
        assertEquals(20, run(List.of(new ConstI32(4),  new ConstI32(5),  new I32Mul())));
    }

    @Test void dividesSigned() {
        assertEquals(3, run(List.of(new ConstI32(10), new ConstI32(3), new I32DivS())));
        assertEquals(0, run(List.of(new ConstI32(1),  new ConstI32(3), new I32DivS())));
        assertEquals(1, run(List.of(new ConstI32(4),  new ConstI32(4), new I32DivS())));
    }

    @Test void divisionTruncatesTowardZeroForAllSignCombinations() {
        assertEquals(-3, run(List.of(new ConstI32(-10), new ConstI32(3),  new I32DivS())));
        assertEquals(-3, run(List.of(new ConstI32(10),  new ConstI32(-3), new I32DivS())));
        assertEquals(3,  run(List.of(new ConstI32(-10), new ConstI32(-3), new I32DivS())));
    }

    @Test void remainderSigned() {
        assertEquals(1, run(List.of(new ConstI32(10), new ConstI32(3), new I32RemS())));
    }

    @Test void divideByZeroTraps() {
        assertThrows(ArithmeticException.class, () -> run(List.of(new ConstI32(5), new ConstI32(0), new I32DivS())));
        assertThrows(ArithmeticException.class, () -> run(List.of(new ConstI32(0), new ConstI32(0), new I32DivS())));
    }

    @Test void divideMinValueByNegOneTraps() {
        assertThrows(ArithmeticException.class, () ->
                run(List.of(new ConstI32(Integer.MIN_VALUE), new ConstI32(-1), new I32DivS())));
    }

    @Test void remainderByZeroTraps() {
        assertThrows(ArithmeticException.class, () -> run(List.of(new ConstI32(5), new ConstI32(0), new I32RemS())));
    }

    @Test void remainderKeepsSignOfDividend() {
        assertEquals(1,  run(List.of(new ConstI32(10),  new ConstI32(3),  new I32RemS())));
        assertEquals(-1, run(List.of(new ConstI32(-10), new ConstI32(3),  new I32RemS())));
        assertEquals(1,  run(List.of(new ConstI32(10),  new ConstI32(-3), new I32RemS())));
        assertEquals(-1, run(List.of(new ConstI32(-10), new ConstI32(-3), new I32RemS())));
    }

    @Test void multiplicationWrapsModulo32Bit() {
        assertEquals(0,                run(List.of(new ConstI32(65536),          new ConstI32(65536), new I32Mul())));
        assertEquals(-2,               run(List.of(new ConstI32(Integer.MAX_VALUE), new ConstI32(2), new I32Mul())));
        assertEquals(Integer.MIN_VALUE,run(List.of(new ConstI32(Integer.MIN_VALUE), new ConstI32(-1), new I32Mul())));
    }

    // ── Comparisons ───────────────────────────────────────────────────────────

    @Test void eqzReturnsTrueForZero()    { assertEquals(1, run(List.of(new ConstI32(0),  new I32Eqz()))); }
    @Test void eqzReturnsFalseForNonZero(){ assertEquals(0, run(List.of(new ConstI32(42), new I32Eqz()))); }

    @Test void eqReturnOneForEqualValues()    { assertEquals(1, run(List.of(new ConstI32(5), new ConstI32(5), new I32Eq()))); }
    @Test void eqReturnZeroForDifferentValues(){ assertEquals(0, run(List.of(new ConstI32(5), new ConstI32(6), new I32Eq()))); }
    @Test void neReturnOneForDifferentValues() { assertEquals(1, run(List.of(new ConstI32(5), new ConstI32(6), new I32Ne()))); }
    @Test void neReturnZeroForEqualValues()    { assertEquals(0, run(List.of(new ConstI32(5), new ConstI32(5), new I32Ne()))); }
    @Test void ltSReturnOneWhenLess()      { assertEquals(1, run(List.of(new ConstI32(3), new ConstI32(5), new I32LtS()))); }
    @Test void ltSReturnZeroWhenNotLess()  { assertEquals(0, run(List.of(new ConstI32(5), new ConstI32(3), new I32LtS()))); }
    @Test void gtSReturnOneWhenGreater()   { assertEquals(1, run(List.of(new ConstI32(5), new ConstI32(3), new I32GtS()))); }
    @Test void gtSReturnZeroWhenNotGreater(){ assertEquals(0, run(List.of(new ConstI32(3), new ConstI32(5), new I32GtS()))); }

    @Test void leSReturnOneWhenLessOrEqual() {
        assertEquals(1, run(List.of(new ConstI32(3), new ConstI32(3), new I32LeS())));
        assertEquals(1, run(List.of(new ConstI32(2), new ConstI32(3), new I32LeS())));
    }

    @Test void geSReturnOneWhenGreaterOrEqual() {
        assertEquals(1, run(List.of(new ConstI32(3), new ConstI32(3), new I32GeS())));
        assertEquals(1, run(List.of(new ConstI32(4), new ConstI32(3), new I32GeS())));
    }

    // ── Locals ────────────────────────────────────────────────────────────────

    @Test void localGetReturnsParam() {
        assertEquals(42, invoke(fn1(List.of(new LocalGet(0))), List.of(WasmValue.i32(42))).asI32());
    }

    @Test void secondParamAccessible() {
        FunctionDef fn = new FunctionDef(
                new FunctionType(List.of(ValueType.I32, ValueType.I32), ValueType.I32),
                List.of(),
                List.of(new LocalGet(1)));
        assertEquals(20, invoke(fn, List.of(WasmValue.i32(10), WasmValue.i32(20))).asI32());
    }

    @Test void extraLocalInitializedToZero() {
        FunctionDef fn = new FunctionDef(
                new FunctionType(List.of(), ValueType.I32),
                List.of(ValueType.I32),
                List.of(new LocalGet(0)));
        assertEquals(0, invoke(fn, List.of()).asI32());
    }

    @Test void localSetAndGet() {
        FunctionDef fn = new FunctionDef(
                new FunctionType(List.of(ValueType.I32), ValueType.I32),
                List.of(ValueType.I32),
                List.of(new ConstI32(99), new LocalSet(1), new LocalGet(1)));
        assertEquals(99, invoke(fn, List.of(WasmValue.i32(0))).asI32());
    }

    @Test void localTeeSetAndLeaveOnStack() {
        FunctionDef fn = new FunctionDef(
                new FunctionType(List.of(), ValueType.I32),
                List.of(ValueType.I32),
                List.of(new ConstI32(77), new LocalTee(0), new ConstI32(1), new I32Add()));
        assertEquals(78, invoke(fn, List.of()).asI32());
    }

    // ── Control flow: block ───────────────────────────────────────────────────

    @Test void blockRunsToCompletion() {
        assertEquals(42, run(List.of(new Block(List.of(new ConstI32(42))))));
    }

    @Test void brExitsBlock() {
        assertEquals(42, run(List.of(new Block(List.of(new Br(0))), new ConstI32(42))));
    }

    @Test void brIfExitsBlockWhenConditionTrue() {
        assertEquals(42, run(List.of(
                new Block(List.of(new ConstI32(1), new BrIf(0))),
                new ConstI32(42))));
    }

    @Test void brIfContinuesWhenConditionFalse() {
        assertEquals(42, run(List.of(
                new Block(List.of(new ConstI32(0), new BrIf(0), new ConstI32(42))))));
    }

    @Test void nestedBlocksBrTargetsOuter() {
        assertEquals(42, run(List.of(
                new Block(List.of(
                        new Block(List.of(new Br(1))),
                        new ConstI32(99)
                )),
                new ConstI32(42))));
    }

    @Test void brIfTrueUnwindsStackToBlockEntryHeight() {
        assertEquals(8, run(List.of(
                new ConstI32(7),
                new Block(List.of(new ConstI32(99), new ConstI32(1), new BrIf(0))),
                new ConstI32(1),
                new I32Add())));
    }

    @Test void brUnwindsStackToBlockEntryHeight() {
        assertEquals(8, run(List.of(
                new ConstI32(7),
                new Block(List.of(new ConstI32(99), new Br(0))),
                new ConstI32(1),
                new I32Add())));
    }

    // ── Control flow: loop ────────────────────────────────────────────────────

    @Test void emptyLoopBodyExitsNormally() {
        assertEquals(42, run(List.of(new ConstI32(42), new Loop(List.of()))));
    }

    @Test void loopSumsZeroToFour() {
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
        assertEquals(10, invoke(fn, List.of()).asI32());
    }

    // ── Control flow: if ──────────────────────────────────────────────────────

    @Test void ifRunsThenBodyWhenTrue() {
        assertEquals(42, run(List.of(
                new ConstI32(1),
                new If(List.of(new ConstI32(42)), List.of(new ConstI32(0))))));
    }

    @Test void ifRunsElseBodyWhenFalse() {
        assertEquals(99, run(List.of(
                new ConstI32(0),
                new If(List.of(new ConstI32(42)), List.of(new ConstI32(99))))));
    }

    @Test void ifWithEmptyElseAndFalseConditionDoesNothing() {
        assertEquals(42, run(List.of(
                new ConstI32(42),
                new ConstI32(0),
                new If(List.of(new ConstI32(99)), List.of()))));
    }

    @Test void brInsideIfExitsIf() {
        assertEquals(42, run(List.of(
                new ConstI32(1),
                new If(List.of(new Br(0), new ConstI32(99)), List.of()),
                new ConstI32(42))));
    }

    // ── Control flow: return ──────────────────────────────────────────────────

    @Test void returnExitsEarlyWithValueOnStack() {
        assertEquals(42, run(List.of(new ConstI32(42), new Return(), new ConstI32(99))));
    }

    @Test void returnInsideBlockUnwindsAll() {
        assertEquals(42, run(List.of(
                new Block(List.of(new ConstI32(42), new Return(), new ConstI32(99))),
                new ConstI32(0))));
    }

    @Test void returnInsideLoopUnwindsAll() {
        assertEquals(42, run(List.of(
                new Block(List.of(new Loop(List.of(new ConstI32(42), new Return())))),
                new ConstI32(0))));
    }

    // ── Nop / Unreachable ─────────────────────────────────────────────────────

    @Test void nopDoesNothing() {
        assertEquals(42, run(List.of(new Nop(), new ConstI32(42), new Nop())));
    }

    @Test void unreachableTraps() {
        assertThrows(RuntimeException.class, () -> run(List.of(new Unreachable())));
    }

    @Test void unreachableIsNotReachedWhenBranchSkipsIt() {
        assertEquals(42, run(List.of(
                new Block(List.of(new Br(0), new Unreachable())),
                new ConstI32(42))));
    }
}
