package com.group4.interwasm.instruction.i32;

import com.group4.interwasm.model.Instruction;

public record ConstI32(int value) implements Instruction {
}
