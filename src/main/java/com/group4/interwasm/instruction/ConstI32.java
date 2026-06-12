package com.group4.interwasm.instruction;

import com.group4.interwasm.model.Instruction;

public record ConstI32(int value) implements Instruction {
}
