package com.group4.interwasm.instruction.control_flow;

import com.group4.interwasm.model.Instruction;

public record BrIf(int labelIndex) implements Instruction {
}