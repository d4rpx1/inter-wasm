package com.group1.interwasm.runtime;
import com.group1.interwasm.instruction.i32.ConstI32;
import com.group1.interwasm.instruction.arithmetic.I32Add;
import com.group1.interwasm.model.FunctionDef;
import com.group1.interwasm.model.Instruction;

import java.util.List;

/**
 * Interpreter for one single function
 */
public final class Interpreter {
    private final FunctionDef function;

    public Interpreter(FunctionDef functionDef) {
        this.function = functionDef;
    }

    public WasmValue invoke(List<WasmValue> args) {
        Frame frame = new Frame(function, args);

        executeBody(function.body(), frame);

        return frame.operandStack().pop();
    }

    private void executeBody(List<Instruction> body, Frame frame) {
        for (Instruction instruction : body) {
            execute(instruction, frame);
        }
    }

    private void execute(Instruction instruction, Frame frame) {
        // Beware when writing locals: the function parameters are also conted as locals
        // so for example function test(int a)  { int b = 0 } b would be local 1 and a local 0
        // but the c compiler would optimize be to a const anyways, so use wat2wasm if you
        // want to not have to play with the clang optimizer
        switch (instruction) {
            case ConstI32 c -> {
                frame.operandStack().push(WasmValue.i32(c.value()));
            }

            case I32Add ignored -> {
                int b = frame.operandStack().popI32();
                int a = frame.operandStack().popI32();

                frame.operandStack().pushI32(a + b);
            }

            // TODO: add other instructions

            default -> throw new UnsupportedOperationException(
                    "Unsupported instruction: " + instruction
            );
        }
    }
}