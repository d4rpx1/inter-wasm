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
    void readAddFileTest() {
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
}