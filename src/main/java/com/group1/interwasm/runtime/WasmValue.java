package com.group1.interwasm.runtime;

import com.group1.interwasm.model.ValueType;

public record WasmValue(ValueType type, int i32) {

    public static WasmValue i32(int value) {
        return new WasmValue(ValueType.I32, value);
    }

    public int asI32() {
        if (type != ValueType.I32) {
            throw new IllegalStateException("Expected i32 but got " + type);
        }

        return i32;
    }

    public static WasmValue defaultValue(ValueType type) {
        return switch (type) {
            case I32 -> WasmValue.i32(0);
        };
    }
}