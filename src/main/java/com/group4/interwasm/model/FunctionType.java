package com.group4.interwasm.model;

import java.util.List;

public record FunctionType(
        List<ValueType> params,
        ValueType result
) {
    public FunctionType {
        params = List.copyOf(params);
    }

    public boolean hasResult() {
        return result != null;
    }

    public ValueType resultType() {
        if (!hasResult()) {
            throw new IllegalStateException("Function has no result");
        }
        return result;
    }

    public int paramCount() {
        return params.size();
    }
}