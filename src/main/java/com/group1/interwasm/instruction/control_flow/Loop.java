package com.group1.interwasm.instruction.control_flow;

import com.group1.interwasm.model.Instruction;

import java.util.List;

public record Loop(List<Instruction> body) implements Instruction {
    public Loop {
        body = List.copyOf(body);
    }
}