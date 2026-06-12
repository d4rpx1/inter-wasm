package com.group4.interwasm.model;

import java.util.List;

public record FunctionType(
        List<ValueType> params,
        List<ValueType> results
) {
    public FunctionType {
        params = List.copyOf(params);
        results = List.copyOf(results);

        // Optional simplification for MVP:
        // WebAssembly supports multiple results, but you may choose not to.
        if (results.size() > 1) {
            throw new IllegalArgumentException(
                    "This interpreter subset supports at most one result"
            );
        }
    }

    public boolean hasResult() {
        return !results.isEmpty();
    }

    public ValueType resultType() {
        if (results.isEmpty()) {
            throw new IllegalStateException("Function has no result");
        }
        return results.getFirst();
    }

    public int paramCount() {
        return params.size();
    }

    public int resultCount() {
        return results.size();
    }
}