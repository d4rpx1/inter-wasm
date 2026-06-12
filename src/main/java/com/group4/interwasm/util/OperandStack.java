package com.group4.interwasm.util;

import com.group4.interwasm.runtime.WasmValue;

import java.util.ArrayDeque;
import java.util.Deque;

public final class OperandStack {
    private final Deque<WasmValue> values = new ArrayDeque<>();

    public void push(WasmValue value) {
        values.push(value);
    }

    public WasmValue pop() {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Operand stack underflow");
        }

        return values.pop();
    }

    public WasmValue peek() {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Operand stack underflow");
        }

        return values.peek();
    }

    public int popI32() {
        return pop().asI32();
    }

    public void pushI32(int value) {
        push(WasmValue.i32(value));
    }

    public int peekI32() {
        return peek().asI32();
    }

    public int size() {
        return values.size();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public void clear() {
        values.clear();
    }
}