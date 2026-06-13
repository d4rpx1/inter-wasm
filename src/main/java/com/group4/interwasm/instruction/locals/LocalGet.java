package com.group4.interwasm.instruction.locals;


import com.group4.interwasm.model.Instruction;

public record LocalGet(int index) implements Instruction {
}