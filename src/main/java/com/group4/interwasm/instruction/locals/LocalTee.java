package com.group4.interwasm.instruction.locals;

import com.group4.interwasm.model.Instruction;

public record LocalTee(int index) implements Instruction {
}