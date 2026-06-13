package com.group1.interwasm.runtime;

import com.group1.interwasm.model.FunctionDef;
import com.group1.interwasm.model.ValueType;
import com.group1.interwasm.util.OperandStack;

import java.util.ArrayList;
import java.util.List;

public final class Frame {
    private final FunctionDef function;
    private final List<WasmValue> locals;
    private final OperandStack operandStack;

    public Frame(FunctionDef function, List<WasmValue> args) {
        this.function = function;
        this.locals = new ArrayList<>();
        this.operandStack = new OperandStack();

        // TODO: initialize function arguments
    }

    public FunctionDef function() {
        return function;
    }

    public OperandStack operandStack() {
        return operandStack;
    }

    public WasmValue getLocal(int index) {
        checkLocalIndex(index);
        return locals.get(index);
    }

    public void setLocal(int index, WasmValue value) {
        checkLocalIndex(index);

        ValueType expectedType = localType(index);
        if (value.type() != expectedType) {
            throw new IllegalArgumentException(
                    "Local " + index + " expected " + expectedType
                            + " but got " + value.type()
            );
        }

        locals.set(index, value);
    }

    public int localCount() {
        return locals.size();
    }

    private void checkLocalIndex(int index) {
        if (index < 0 || index >= locals.size()) {
            throw new IllegalArgumentException("Unknown local index: " + index);
        }
    }

    private ValueType localType(int index) {
        int paramCount = function.params().size();

        if (index < paramCount) {
            return function.params().get(index);
        }

        return function.locals().get(index - paramCount);
    }
}