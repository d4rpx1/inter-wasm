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
import com.group1.interwasm.runtime.Interpreter2;
import com.group1.interwasm.runtime.WasmValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    static double timeInterpreter(FunctionDef fn, int input, int warmup, int measure) {
        Interpreter interp = new Interpreter(fn);
<<<<<<< Updated upstream

        // Warm up -> lets the JVM JIT-compile the interpreter itself
=======
>>>>>>> Stashed changes
        for (int i = 0; i < warmup; i++) interp.invoke(List.of(WasmValue.i32(input)));
        long t0 = System.nanoTime();
        for (int i = 0; i < measure; i++) interp.invoke(List.of(WasmValue.i32(input)));
        return (double)(System.nanoTime() - t0) / measure / 1000.0;
    }

    static double timeInterpreter2(FunctionDef fn, int input, int warmup, int measure) {
        Interpreter2 interp = new Interpreter2(fn);
        for (int i = 0; i < warmup; i++) interp.invoke(List.of(WasmValue.i32(input)));
        long t0 = System.nanoTime();
        for (int i = 0; i < measure; i++) interp.invoke(List.of(WasmValue.i32(input)));
        return (double)(System.nanoTime() - t0) / measure / 1000.0;
    }

<<<<<<< Updated upstream
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
=======
    /** Spawns `node benchmark.js` and parses "label ... X.XXX µs/call" lines. */
    static Map<String, Double> runNodeBenchmark() {
        Map<String, Double> results = new LinkedHashMap<>();
        try {
            Process proc = new ProcessBuilder("node", "benchmark.js")
                    .directory(new File("."))
                    .redirectErrorStream(true)
                    .start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (!line.contains("µs/call")) continue;
                    String[] parts = line.trim().split("\\s+");
                    String label = parts[0];
                    for (int i = 0; i < parts.length; i++) {
                        if (parts[i].equals("µs/call")) {
                            results.put(label, Double.parseDouble(parts[i - 1]));
                            break;
                        }
                    }
                }
            }
            proc.waitFor();
        } catch (Exception e) {
            System.err.println("Warning: could not run node benchmark.js — " + e.getMessage());
        }
        return results;
>>>>>>> Stashed changes
    }

    public static void main(String[] args) {
        final int WARMUP = 10_000, MEASURE = 100_000;

        record Case(String label, FunctionDef fn, int input, int result) {}
        List<Case> cases = List.of(
                new Case("add_with_local(5)",  addWithLocal(),  5,  10),
                new Case("simple_loop(0)",     simpleLoop(),    0,  45),
                new Case("fibonacci(10)",      fibonacci(),    10,  55)
        );

        System.out.println("Warming up and measuring Java interpreters...");
        double[] us1 = new double[cases.size()];
        double[] us2 = new double[cases.size()];
        for (int i = 0; i < cases.size(); i++) {
            Case c = cases.get(i);
            us1[i] = timeInterpreter(c.fn(), c.input(), WARMUP, MEASURE);
            us2[i] = timeInterpreter2(c.fn(), c.input(), WARMUP, MEASURE);
        }

        System.out.println("Running Node.js/V8 benchmark...");
        Map<String, Double> node = runNodeBenchmark();

        System.out.println();
        System.out.println("=== WebAssembly VM comparison: tree-walking interpreter vs V8 JIT ===");
        System.out.println("  Warm-up: 10,000 calls  |  Measure: 100,000 calls\n");

        String sep = "─".repeat(86);
        System.out.println(sep);
        System.out.printf("  %-22s │ %11s │ %11s │ %11s │ %8s │ %8s%n",
                "Function", "Interp. (µs)", "Interp2 (µs)", "V8 JIT (µs)", "v1→v2", "v2→V8");
        System.out.println(sep);

        for (int i = 0; i < cases.size(); i++) {
            Case c = cases.get(i);
            Double nodeUs = node.get(c.label());
            String nodeStr  = nodeUs != null ? String.format("%11.3f", nodeUs) : "        N/A";
            String v2vsV8   = nodeUs != null ? String.format("%7.1f×", us2[i] / nodeUs) : "      N/A";
            System.out.printf("  %-22s │ %11.3f │ %11.3f │ %s │ %7.1f× │ %s%n",
                    c.label() + " →" + c.result(),
                    us1[i], us2[i], nodeStr,
                    us1[i] / us2[i], v2vsV8);
        }
        System.out.println(sep);
        System.out.println();
        System.out.println("  v1→v2  : speedup from int[] stack/locals + in-place ops (eliminates invokevirtual + heap alloc)");
        System.out.println("  v2→V8  : remaining gap = tree dispatch overhead (recursive executeBody + Iterator + typeSwitch)");
        System.out.println("           V8 JIT compiles .wasm to native — essentially pure Nutzlast, no interpreter overhead");
    }
}
