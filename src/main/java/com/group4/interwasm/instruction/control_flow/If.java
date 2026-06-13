package com.group4.interwasm.instruction.control_flow;

import com.group4.interwasm.model.Instruction;

import java.util.List;

public record If(
        List<Instruction> thenBody,
        List<Instruction> elseBody
) implements Instruction {
    public If {
        thenBody = List.copyOf(thenBody);
        elseBody = List.copyOf(elseBody);
    }
}