package com.group1.interwasm.runtime;

import com.group1.interwasm.model.FunctionDef;
import com.group1.interwasm.model.Instruction;

import java.util.List;

class Interpreter3Test extends InterpreterContractTest {

    @Override
    int run(List<Instruction> body) {
        return new Interpreter3(fn(body)).invoke(List.of()).asI32();
    }

    @Override
    WasmValue invoke(FunctionDef fn, List<WasmValue> args) {
        return new Interpreter3(fn).invoke(args);
    }
}
