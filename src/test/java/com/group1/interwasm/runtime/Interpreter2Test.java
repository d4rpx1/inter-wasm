package com.group1.interwasm.runtime;

import com.group1.interwasm.model.FunctionDef;
import com.group1.interwasm.model.Instruction;

import java.util.List;

class Interpreter2Test extends InterpreterContractTest {

    @Override
    int run(List<Instruction> body) {
        return new Interpreter2(fn(body)).invoke(List.of()).asI32();
    }

    @Override
    WasmValue invoke(FunctionDef fn, List<WasmValue> args) {
        return new Interpreter2(fn).invoke(args);
    }
}
