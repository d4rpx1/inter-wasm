package com.group1.interwasm;

import com.group1.interwasm.model.ValueType;
import com.group1.interwasm.runtime.WasmValue;
import com.group1.interwasm.util.FilesUtil;
import org.junit.jupiter.api.Test;

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
        assertEquals(15, returnVal.i32()); // add compiled by clang adds 10 to the first parameter
    }

    @Test
    void testSimpleLoop() {
        String wasmFilePath = FilesUtil.getPathToResourceFile("wasm-examples/simple_loop/simple_loop.wasm");
        AtomicReference<WasmValue> funcdefReturn = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmValue returnVal = Main.interpretFile(wasmFilePath, List.of(new WasmValue(ValueType.I32, 5)));
            funcdefReturn.set(returnVal);
        });

        WasmValue returnVal = funcdefReturn.get();

        assertEquals(ValueType.I32, returnVal.type());
        assertEquals(50, returnVal.i32()); // simple_loop(5) returns 50
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
        assertEquals(5, returnVal.i32()); // fibonacci(5) = 5
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
        assertEquals(3, returnVal.i32()); // primes up to 5 are {2, 3, 5} = 3 primes
    }
}