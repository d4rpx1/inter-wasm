package com.group1.interwasm.util;

import com.group1.interwasm.runtime.ExecutionStats;
import com.group1.interwasm.runtime.WasmValue;

import java.util.ArrayDeque;
import java.util.Deque;

public final class OperandStack {
    private final Deque<WasmValue> values = new ArrayDeque<>();
    private ExecutionStats stats;

    public OperandStack() {}

    public OperandStack(ExecutionStats stats) {
        this.stats = stats;
    }

    public void push(WasmValue value) {
        if (stats != null) stats.operandWrites++;
        values.push(value);
    }

    public WasmValue pop() {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Operand stack underflow");
        }
        if (stats != null) stats.operandReads++;
        return values.pop();
    }

    public WasmValue peek() {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Operand stack underflow");
        }
        if (stats != null) stats.operandReads++;
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

    public void truncateTo(int size) {
        while (values.size() > size) {
            values.pop();
        }
    }
}