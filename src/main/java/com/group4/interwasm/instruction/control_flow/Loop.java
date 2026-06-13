package com.group4.interwasm.instruction.control_flow;

import com.group4.interwasm.model.Instruction;

import java.util.List;

public record Loop(List<Instruction> body) implements Instruction {
    public Loop {
        body = List.copyOf(body);
    }
}