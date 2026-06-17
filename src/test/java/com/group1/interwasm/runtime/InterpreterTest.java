package com.group1.interwasm.runtime;

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

    @Override
    WasmValue invoke(FunctionDef fn, List<WasmValue> args) {
        return new Interpreter(fn).invoke(args);
    }

    // ── Interpreter-specific validation (not part of the shared contract) ─────

    @Test void invokeRejectsWrongArgumentCount() {
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
}
