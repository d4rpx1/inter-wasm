package com.group1.interwasm.runtime;

public sealed interface ControlSignal permits ControlSignal.Next, ControlSignal.Branch, ControlSignal.FunctionReturn {
    record Next() implements ControlSignal {}
    record Branch(int depth) implements ControlSignal {}
    record FunctionReturn() implements ControlSignal {}

    ControlSignal NEXT = new Next();
    ControlSignal RETURN = new FunctionReturn();
}
