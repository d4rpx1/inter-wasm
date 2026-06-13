package com.group1.interwasm;

import com.group1.interwasm.bytecode.WasmReader;
import com.group1.interwasm.model.FunctionDef;
import com.group1.interwasm.model.ValueType;
import com.group1.interwasm.runtime.Interpreter;
import com.group1.interwasm.runtime.WasmValue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.group1.interwasm.util.FilesUtil.getPathToResourceFile;

public class Main {

    static void main() {
        // TODO maybe use a command argument for file path
        String path = getPathToResourceFile("wasm-examples/add_with_local/add_with_local.wasm");

        IO.println("Executing " + path);

        WasmValue returnValue;

        try {
            returnValue = interpretFile(path, List.of(new WasmValue(ValueType.I32, 5)));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        IO.println("Done. Return value " + returnValue.i32());
    }

    /**
     * Interprets the first function of the wasm file stored in path with the given arguments.
     * Extra function was created instead of just putting everything in main so we can write tests
     *
     * @param path path to the wasm file
     * @param args arguments to the wasm function
     * @return return value of the function
     * @throws IOException when the file is not found
     */
    public static WasmValue interpretFile(String path, List<WasmValue> args) throws IOException {
        WasmReader reader = new WasmReader(new File(path));

        FunctionDef functionDef = reader.readFunctionDefinition();

        Interpreter interpreter = new Interpreter(functionDef);

        return interpreter.invoke(args);
    }
}
