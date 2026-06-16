package com.group1.interwasm;

import com.group1.interwasm.instruction.arithmetic.*;
import com.group1.interwasm.instruction.comparisons.*;
import com.group1.interwasm.instruction.control_flow.*;
import com.group1.interwasm.instruction.i32.ConstI32;
import com.group1.interwasm.instruction.locals.*;
import com.group1.interwasm.model.FunctionDef;
import com.group1.interwasm.model.FunctionType;
import com.group1.interwasm.model.ValueType;
import com.group1.interwasm.runtime.ExecutionStats;
import com.group1.interwasm.runtime.Interpreter;
import com.group1.interwasm.runtime.WasmValue;

import java.util.List;

/**
 * Java interpreter benchmark -> compare against benchmark.js (Node.js / V8 JIT).
 *
 * Run:  mvn compile exec:java -Dexec.mainClass=com.group1.interwasm.Benchmark
 *
 * Reports:
 *   - µs/call and calls/s  (compare timing against Node.js to see interpreter overhead)
 *   - dispatches, operandReads, operandWrites for one representative call
 *     (maps to the Dispatch / Operandenzugriff / Nutzlast breakdown from the slides)
 */
public class Benchmark {

    //  Program definitions 

    /**
     * add_with_local(x) = x + 5
     * Instructions: const 5, local.set 1, local.get 0, local.get 1, add
     */
    static FunctionDef addWithLocal() {
        return new FunctionDef(
                new FunctionType(List.of(ValueType.I32), ValueType.I32),
                List.of(ValueType.I32),
                List.of(
                        new ConstI32(5), new LocalSet(1),
                        new LocalGet(0), new LocalGet(1), new I32Add()
                ));
    }

    /**
     * simple_loop(x) = x + 0 + 1 + ... + 9  (= x + 45)
     * block { loop { i>=10 → br 1; x+=i; i+=1; br 0 } }; local.get x
     */
    static FunctionDef simpleLoop() {
        return new FunctionDef(
                new FunctionType(List.of(ValueType.I32), ValueType.I32),
                List.of(ValueType.I32),          // local 1 = i
                List.of(
                        new Block(List.of(
                                new Loop(List.of(
                                        new LocalGet(1), new ConstI32(10), new I32GeS(), new BrIf(1),
                                        new LocalGet(0), new LocalGet(1), new I32Add(), new LocalSet(0),
                                        new LocalGet(1), new ConstI32(1), new I32Add(), new LocalSet(1),
                                        new Br(0)
                                ))
                        )),
                        new LocalGet(0)
                ));
    }

    /**
     * fibonacci(n) -> iterative, returns fib(n)
     * Locals: 0=n(param), 1=a, 2=b, 3=i, 4=tmp
     */
    static FunctionDef fibonacci() {
        return new FunctionDef(
                new FunctionType(List.of(ValueType.I32), ValueType.I32),
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32), // a,b,i,tmp
                List.of(
                        // a=0, b=1, i=0
                        new ConstI32(0), new LocalSet(1),
                        new ConstI32(1), new LocalSet(2),
                        new ConstI32(0), new LocalSet(3),
                        new Block(List.of(
                                new Loop(List.of(
                                        // if i >= n: break
                                        new LocalGet(3), new LocalGet(0), new I32GeS(), new BrIf(1),
                                        // tmp = a + b
                                        new LocalGet(1), new LocalGet(2), new I32Add(), new LocalSet(4),
                                        // a = b
                                        new LocalGet(2), new LocalSet(1),
                                        // b = tmp
                                        new LocalGet(4), new LocalSet(2),
                                        // i++
                                        new LocalGet(3), new ConstI32(1), new I32Add(), new LocalSet(3),
                                        new Br(0)
                                ))
                        )),
                        new LocalGet(1)
                ));
    }

    //  Benchmark runner 

    static void bench(String label, FunctionDef fn, int input, int warmup, int measure) {
        Interpreter interp = new Interpreter(fn);

        // Warm up -> lets the JVM JIT-compile the interpreter itself
        for (int i = 0; i < warmup; i++) interp.invoke(List.of(WasmValue.i32(input)));

        long t0 = System.nanoTime();
        for (int i = 0; i < measure; i++) interp.invoke(List.of(WasmValue.i32(input)));
        long ns = System.nanoTime() - t0;

        double usPerCall  = (double) ns / measure / 1000.0;
        double callsPerSec = 1e9 / ((double) ns / measure);

        // Collect stats for one representative call
        ExecutionStats stats = new ExecutionStats();
        Interpreter statsInterp = new Interpreter(fn, stats);
        int result = statsInterp.invoke(List.of(WasmValue.i32(input))).asI32();

        System.out.printf("%-30s input=%5d  result=%6d  %8.3f µs/call  %12s calls/s%n",
                label, input, result,
                usPerCall,
                String.format("%,.0f", callsPerSec));
        System.out.printf("  └ dispatches=%-6d  operandReads=%-6d  operandWrites=%-6d  operandOps/dispatch=%.2f%n%n",
                stats.dispatches, stats.operandReads, stats.operandWrites,
                stats.dispatches == 0 ? 0.0 : (double) stats.totalOperandOps() / stats.dispatches);
    }

    public static void main(String[] args) {
        System.out.println("=== Java tree-walking interpreter benchmark ===\n");
        System.out.println("Warm-up: 10,000 calls  |  Measure: 100,000 calls\n");

        bench("add_with_local(5)",  addWithLocal(),  5,  10_000, 100_000);
        bench("simple_loop(0)",     simpleLoop(),    0,  10_000, 100_000);
        bench("fibonacci(10)",      fibonacci(),     10, 10_000, 100_000);

        System.out.println("Compare µs/call against Node.js (node benchmark.js).");
        System.out.println("The ratio ≈ total interpreter overhead (Dispatch + Operandenzugriff).");
    }
}
