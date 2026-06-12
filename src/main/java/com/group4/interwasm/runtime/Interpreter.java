package com.group4.interwasm.runtime;
import com.group4.interwasm.instruction.ConstI32;
import com.group4.interwasm.instruction.I32Add;
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
        switch (instruction) {
            case ConstI32 c -> {
                frame.operandStack().push(WasmValue.i32(c.value()));
            }

            case I32Add ignored -> {
                int b = frame.operandStack().popI32();
                int a = frame.operandStack().popI32();

                frame.operandStack().pushI32(a + b);
            }

            default -> throw new UnsupportedOperationException(
                    "Unsupported instruction: " + instruction
            );
        }
    }
}