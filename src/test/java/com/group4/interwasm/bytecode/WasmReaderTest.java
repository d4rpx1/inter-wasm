package com.group4.interwasm.bytecode;

import com.group4.interwasm.instruction.ConstI32;
import com.group4.interwasm.instruction.I32Add;
import com.group4.interwasm.instruction.LocalGet;
import com.group4.interwasm.model.FunctionDef;
import com.group4.interwasm.model.ValueType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class WasmReaderTest {
    @Test
    void readAddFileTest() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL resourceUrl = classloader.getResource("wasm-examples/add/add.wasm");

        if (resourceUrl == null) {
            throw new IllegalArgumentException("File not found!");
        }

        AtomicReference<FunctionDef> functionDefAtomic = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmReader reader = new WasmReader(new File(resourceUrl.toURI()));
            functionDefAtomic.set(reader.readFunctionDefinition());
        });

        FunctionDef functionDef = functionDefAtomic.get();

        assertEquals(ValueType.I32, functionDef.type().resultType()); // return type is integer
        assertEquals(List.of(ValueType.I32), functionDef.type().params()); // one integer parameter parameters (a from add.wasm)
        assertEquals(List.of(ValueType.I32), functionDef.locals()); // one integer local (b from add.wasm)

        // TODO Maybe we must implement IComparable right
        // so the check wheter localget(0) == localget(0) (two different objects) works
        assertEquals(
                List.of(
                        new I32Add(),
                        new LocalGet(0),
                        new ConstI32(5)
                ), functionDef.body()
        );


    }
}