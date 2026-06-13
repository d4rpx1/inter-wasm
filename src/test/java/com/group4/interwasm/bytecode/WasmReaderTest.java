package com.group4.interwasm.bytecode;

import com.group4.interwasm.instruction.ConstI32;
import com.group4.interwasm.instruction.I32Add;
import com.group4.interwasm.instruction.LocalGet;
import com.group4.interwasm.instruction.LocalSet;
import com.group4.interwasm.model.FunctionDef;
import com.group4.interwasm.model.ValueType;
import com.group4.interwasm.util.FilesUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class WasmReaderTest {
    @Test
    void readAddGeneratedByClangTest() {
        String wasmFilePath = FilesUtil.getPathToResourceFile("wasm-examples/add/add.wasm");
        AtomicReference<FunctionDef> functionDefAtomic = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmReader reader = new WasmReader(new File(wasmFilePath));
            functionDefAtomic.set(reader.readFunctionDefinition());
        });

        FunctionDef functionDef = functionDefAtomic.get();

        assertEquals(ValueType.I32, functionDef.type().resultType()); // return type is integer
        assertEquals(List.of(ValueType.I32), functionDef.type().params()); // one int parameter
        assertEquals(List.of(), functionDef.locals()); // no locals, because they get optimized away by clang

        assertEquals(
                List.of(
                        new LocalGet(0),
                        new ConstI32(10),
                        new I32Add()
                ), functionDef.body()
        );
    }

    @Test
    void readAddWithLocalTest() {
        String wasmFilePath = FilesUtil.getPathToResourceFile("wasm-examples/add_with_local/add_with_local.wasm");
        AtomicReference<FunctionDef> functionDefAtomic = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmReader reader = new WasmReader(new File(wasmFilePath));
            functionDefAtomic.set(reader.readFunctionDefinition());
        });

        FunctionDef functionDef = functionDefAtomic.get();

        assertEquals(ValueType.I32, functionDef.type().resultType()); // return type is integer
        assertEquals(List.of(ValueType.I32), functionDef.type().params()); // one integer parameter parameters (a from add.wasm)
        assertEquals(List.of(ValueType.I32), functionDef.locals()); // one integer local (b from add.wasm)

        assertEquals(
                List.of(
                        new ConstI32(5),
                        new LocalSet(1),
                        new LocalGet(0),
                        new LocalGet(1),
                        new I32Add()
                ), functionDef.body()
        );
    }

    @Test
    void readCountPrimes() {
        String wasmFilePath = FilesUtil.getPathToResourceFile("wasm-examples/count_primes/count_primes.wasm");
        AtomicReference<FunctionDef> functionDefAtomic = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmReader reader = new WasmReader(new File(wasmFilePath));
            functionDefAtomic.set(reader.readFunctionDefinition());
        });

        FunctionDef functionDef = functionDefAtomic.get();

        assertEquals(ValueType.I32, functionDef.type().resultType()); // return type is integer
        assertEquals(List.of(ValueType.I32), functionDef.type().params()); // one integer parameter parameters (a from add.wasm)
        assertEquals(List.of(ValueType.I32), functionDef.locals()); // one integer local (b from add.wasm)

        // TODO: add assertEquals, do this after Interpreter part is done
        /*assertEquals(
                List.of(
                        new ConstI32(5),
                        new LocalSet(1),
                        new LocalGet(0),
                        new LocalGet(1),
                        new I32Add()
                ), functionDef.body()
        );*/
    }

    @Test
    void readFibonacci() {
        String wasmFilePath = FilesUtil.getPathToResourceFile("wasm-examples/fibonacci/fibonacci.wasm");
        AtomicReference<FunctionDef> functionDefAtomic = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmReader reader = new WasmReader(new File(wasmFilePath));
            functionDefAtomic.set(reader.readFunctionDefinition());
        });

        FunctionDef functionDef = functionDefAtomic.get();

        assertEquals(ValueType.I32, functionDef.type().resultType()); // return type is integer
        assertEquals(List.of(ValueType.I32), functionDef.type().params()); // one integer parameter parameters (a from add.wasm)
        assertEquals(List.of(ValueType.I32), functionDef.locals()); // one integer local (b from add.wasm)

        // TODO: add assertEquals, do this after Interpreter part is done
        /*assertEquals(
                List.of(
                        new ConstI32(5),
                        new LocalSet(1),
                        new LocalGet(0),
                        new LocalGet(1),
                        new I32Add()
                ), functionDef.body()
        );*/
    }

    @Test
    void readSimpleLoop() {
        String wasmFilePath = FilesUtil.getPathToResourceFile("wasm-examples/simple_loop/simple_loop.wasm");
        AtomicReference<FunctionDef> functionDefAtomic = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmReader reader = new WasmReader(new File(wasmFilePath));
            functionDefAtomic.set(reader.readFunctionDefinition());
        });

        FunctionDef functionDef = functionDefAtomic.get();

        assertEquals(ValueType.I32, functionDef.type().resultType()); // return type is integer
        assertEquals(List.of(ValueType.I32), functionDef.type().params()); // one integer parameter parameters (a from add.wasm)
        assertEquals(List.of(ValueType.I32), functionDef.locals()); // one integer local (b from add.wasm)

        // TODO: add assertEquals, do this after Interpreter part is done
        /*assertEquals(
                List.of(
                        new ConstI32(5),
                        new LocalSet(1),
                        new LocalGet(0),
                        new LocalGet(1),
                        new I32Add()
                ), functionDef.body()
        );*/
    }
}