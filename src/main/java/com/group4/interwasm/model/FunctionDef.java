package com.group4.interwasm.model;

import java.util.List;

public record FunctionDef(
        FunctionType type,
        List<ValueType> locals,
        List<Instruction> body
) {
    public FunctionDef {
        locals = List.copyOf(locals);
        body = List.copyOf(body);
    }

    public List<ValueType> params() {
        return type.params();
    }

    public List<ValueType> results() {
        return type.results();
    }
}