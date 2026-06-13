package com.group4.interwasm;

import com.group4.interwasm.bytecode.WasmReader;
import com.group4.interwasm.model.FunctionDef;
import com.group4.interwasm.model.ValueType;
import com.group4.interwasm.runtime.WasmValue;
import com.group4.interwasm.util.FilesUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @Test
    void testAddWithLocalVariable() {
        String wasmFilePath = FilesUtil.getPathToResourceFile("wasm-examples/add_with_local/add_with_local.wasm");
        AtomicReference<WasmValue> funcdefReturn = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmValue returnVal = Main.interpretFile(wasmFilePath, List.of(new WasmValue(ValueType.I32, 5)));
            funcdefReturn.set(returnVal);
        });

        WasmValue returnVal = funcdefReturn.get();

        assertEquals(ValueType.I32, returnVal.type());
        assertEquals(10, returnVal.i32()); // basically add_with_local adds 5 to the first parameter
    }

    @Test
    void testAddCompiledUsingClang() {
        String wasmFilePath = FilesUtil.getPathToResourceFile("wasm-examples/add/add.wasm");
        AtomicReference<WasmValue> funcdefReturn = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmValue returnVal = Main.interpretFile(wasmFilePath, List.of(new WasmValue(ValueType.I32, 5)));
            funcdefReturn.set(returnVal);
        });

        WasmValue returnVal = funcdefReturn.get();

        assertEquals(ValueType.I32, returnVal.type());
        assertEquals(10, returnVal.i32()); // basically add_with_local adds 5 to the first parameter
    }

    @Test
    void testSimpleLoop() {
        String wasmFilePath = FilesUtil.getPathToResourceFile("wasm-examples/simple_loop/loop.wasm");
        AtomicReference<WasmValue> funcdefReturn = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmValue returnVal = Main.interpretFile(wasmFilePath, List.of(new WasmValue(ValueType.I32, 5)));
            funcdefReturn.set(returnVal);
        });

        WasmValue returnVal = funcdefReturn.get();

        assertEquals(ValueType.I32, returnVal.type());
        assertEquals(10, returnVal.i32()); // basically add_with_local adds 5 to the first parameter
    }

    @Test
    void testFibonacci() {
        String wasmFilePath = FilesUtil.getPathToResourceFile("wasm-examples/fibonacci/fibonacci.wasm");
        AtomicReference<WasmValue> funcdefReturn = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmValue returnVal = Main.interpretFile(wasmFilePath, List.of(new WasmValue(ValueType.I32, 5)));
            funcdefReturn.set(returnVal);
        });

        WasmValue returnVal = funcdefReturn.get();

        assertEquals(ValueType.I32, returnVal.type());
        assertEquals(10, returnVal.i32()); // basically add_with_local adds 5 to the first parameter
    }

    @Test
    void testCountPrimes() {
        String wasmFilePath = FilesUtil.getPathToResourceFile("wasm-examples/count_primes/count_primes.wasm");
        AtomicReference<WasmValue> funcdefReturn = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmValue returnVal = Main.interpretFile(wasmFilePath, List.of(new WasmValue(ValueType.I32, 5)));
            funcdefReturn.set(returnVal);
        });

        WasmValue returnVal = funcdefReturn.get();

        assertEquals(ValueType.I32, returnVal.type());
        assertEquals(10, returnVal.i32()); // basically add_with_local adds 5 to the first parameter
    }
}