package com.group1.interwasm.instruction.locals;


import com.group1.interwasm.model.Instruction;

public record LocalGet(int index) implements Instruction {
}