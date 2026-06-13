package com.group1.interwasm.instruction.locals;

import com.group1.interwasm.model.Instruction;

public record LocalTee(int index) implements Instruction {
}