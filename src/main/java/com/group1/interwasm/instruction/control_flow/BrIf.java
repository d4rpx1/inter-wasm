package com.group1.interwasm.instruction.control_flow;

import com.group1.interwasm.model.Instruction;

public record BrIf(int labelIndex) implements Instruction {
}