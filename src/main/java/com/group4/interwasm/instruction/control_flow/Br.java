package com.group4.interwasm.instruction.control_flow;


import com.group4.interwasm.model.Instruction;

public record Br(int labelIndex) implements Instruction {
}
