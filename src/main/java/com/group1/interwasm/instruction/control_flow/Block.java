package com.group1.interwasm.instruction.control_flow;

import com.group1.interwasm.model.Instruction;

import java.util.List;

public record Block(List<Instruction> body) implements Instruction {
    public Block {
        body = List.copyOf(body);
    }
}