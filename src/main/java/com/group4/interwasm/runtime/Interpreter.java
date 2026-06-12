package com.group4.interwasm.runtime;
import com.group4.interwasm.instruction.ConstI32;
import com.group4.interwasm.model.FunctionDef;
import com.group4.interwasm.model.Instruction;

import java.util.ArrayList;
import java.util.List;
import com.group4.interwasm.runtime.Frame;

/**
 * Interpreter for one single function
 */
public final class Interpreter {
    private final FunctionDef function;

    public Interpreter(FunctionDef functionDef) {
        this.function = functionDef;
    }

    public List<WasmValue> invoke(List<WasmValue> args) {
        Frame frame = new Frame(function, args);

        executeBody(function.body(), frame);

        int expectedResults = function.results().size();
        List<WasmValue> results = new ArrayList<>();

        for (int i = 0; i < expectedResults; i++) {
            results.addFirst(frame.operandStack().pop());
        }

        return results;
    }

    private void executeBody(List<Instruction> body, Frame frame) {
        for (Instruction instruction : body) {
            execute(instruction, frame);
        }
    }

    private void execute(Instruction instruction, Frame frame) {
        switch (instruction) {
            case ConstI32 c -> {
                frame.operandStack().push(WasmValue.i32(c.value()));
            }

            default -> throw new UnsupportedOperationException(
                    "Unsupported instruction: " + instruction
            );
        }
    }
}