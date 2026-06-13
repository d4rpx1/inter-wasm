package com.group1.interwasm.instruction.i32;

import com.group1.interwasm.model.Instruction;

public record ConstI32(int value) implements Instruction {
}
