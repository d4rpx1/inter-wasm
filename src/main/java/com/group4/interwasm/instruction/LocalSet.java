package com.group4.interwasm.instruction;

import com.group4.interwasm.model.Instruction;

public record LocalSet(int index) implements Instruction {
}