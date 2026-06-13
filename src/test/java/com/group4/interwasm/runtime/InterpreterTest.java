package com.group4.interwasm.runtime;

import com.group4.interwasm.instruction.i32.ConstI32;
import com.group4.interwasm.instruction.arithmetic.I32Add;
import com.group4.interwasm.model.FunctionDef;
import com.group4.interwasm.model.FunctionType;
import com.group4.interwasm.model.ValueType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InterpreterTest {
    @Test
    void addsTwoI32Values() {
        // 2+3
        FunctionDef fn = new FunctionDef(
                new FunctionType(
                        List.of(), // no params
                        ValueType.I32 // result is integer 32
                ),
                List.of(), // no locals (in wasm, function params are also locals)
                // Instructions
                List.of(
                        new ConstI32(2),
                        new ConstI32(3),
                        new I32Add()
                )
        );

        Interpreter interpreter = new Interpreter(fn);

        // Should multiple results even be supported?
        WasmValue result = interpreter.invoke(List.of());

        assertEquals(5, result.asI32());
    }

    // TODO add tests for loop, blocks, ifs, ... and implement these
}