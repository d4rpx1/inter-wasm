package com.group1.interwasm;

import com.group1.interwasm.instruction.arithmetic.*;
import com.group1.interwasm.instruction.comparisons.*;
import com.group1.interwasm.instruction.control_flow.*;
import com.group1.interwasm.instruction.i32.ConstI32;
import com.group1.interwasm.instruction.locals.*;
import com.group1.interwasm.model.FunctionDef;
import com.group1.interwasm.model.FunctionType;
import com.group1.interwasm.model.ValueType;
import com.group1.interwasm.runtime.Interpreter;
import com.group1.interwasm.runtime.Interpreter2;
import com.group1.interwasm.runtime.Interpreter3;
import com.group1.interwasm.runtime.WasmValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Java interpreter benchmark → compare v1, v2, v3 against Node.js / V8 JIT.
 *
 * Run:  mvn compile exec:java -Dexec.mainClass=com.group1.interwasm.Benchmark
 */
public class Benchmark {

    //  Program definitions 

    /** add_with_local(x) = x + 5 */
    static FunctionDef addWithLocal() {
        return new FunctionDef(
                new FunctionType(List.of(ValueType.I32), ValueType.I32),
                List.of(ValueType.I32),
                List.of(
                        new ConstI32(5), new LocalSet(1),
                        new LocalGet(0), new LocalGet(1), new I32Add()
                ));
    }

    /** simple_loop(x) = x + 0 + 1 + … + 9  (= x + 45) */
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

    /** fibonacci(n) → iterative fib(n); locals: 0=n(param), 1=a, 2=b, 3=i, 4=tmp */
    static FunctionDef fibonacci() {
        return new FunctionDef(
                new FunctionType(List.of(ValueType.I32), ValueType.I32),
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
                List.of(
                        new ConstI32(0), new LocalSet(1),
                        new ConstI32(1), new LocalSet(2),
                        new ConstI32(0), new LocalSet(3),
                        new Block(List.of(
                                new Loop(List.of(
                                        new LocalGet(3), new LocalGet(0), new I32GeS(), new BrIf(1),
                                        new LocalGet(1), new LocalGet(2), new I32Add(), new LocalSet(4),
                                        new LocalGet(2), new LocalSet(1),
                                        new LocalGet(4), new LocalSet(2),
                                        new LocalGet(3), new ConstI32(1), new I32Add(), new LocalSet(3),
                                        new Br(0)
                                ))
                        )),
                        new LocalGet(1)
                ));
    }

    /** count_primes(n) → number of primes in [2..n]; locals: 0=n, 1=count, 2=i, 3=is_prime, 4=j */
    static FunctionDef countPrimes() {
        return new FunctionDef(
                new FunctionType(List.of(ValueType.I32), ValueType.I32),
                List.of(ValueType.I32, ValueType.I32, ValueType.I32, ValueType.I32),
                List.of(
                        new ConstI32(0), new LocalSet(1),
                        new ConstI32(2), new LocalSet(2),
                        new Block(List.of(
                                new Loop(List.of(
                                        new LocalGet(2), new LocalGet(0), new I32GtS(), new BrIf(1),
                                        new ConstI32(1), new LocalSet(3),
                                        new ConstI32(2), new LocalSet(4),
                                        new Block(List.of(
                                                new Loop(List.of(
                                                        new LocalGet(4), new LocalGet(4), new I32Mul(), new LocalGet(2), new I32GtS(), new BrIf(1),
                                                        new LocalGet(2), new LocalGet(4), new I32RemS(), new I32Eqz(),
                                                        new If(List.of(new ConstI32(0), new LocalSet(3), new Br(2)), List.of()),
                                                        new LocalGet(4), new ConstI32(1), new I32Add(), new LocalSet(4),
                                                        new Br(0)
                                                ))
                                        )),
                                        new LocalGet(3),
                                        new If(List.of(new LocalGet(1), new ConstI32(1), new I32Add(), new LocalSet(1)), List.of()),
                                        new LocalGet(2), new ConstI32(1), new I32Add(), new LocalSet(2),
                                        new Br(0)
                                ))
                        )),
                        new LocalGet(1)
                ));
    }

    //  Timing helpers

    static double timeInterpreter(FunctionDef fn, int input, int warmup, int measure) {
        Interpreter interp = new Interpreter(fn);
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

    static double timeInterpreter3(FunctionDef fn, int input, int warmup, int measure) {
        Interpreter3 interp = new Interpreter3(fn);
        for (int i = 0; i < warmup; i++) interp.invoke(List.of(WasmValue.i32(input)));
        long t0 = System.nanoTime();
        for (int i = 0; i < measure; i++) interp.invoke(List.of(WasmValue.i32(input)));
        return (double)(System.nanoTime() - t0) / measure / 1000.0;
    }

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
            System.err.println("Warning: could not run node benchmark.js" + e.getMessage());
        }
        return results;
    }

    // Benchmark case 

    record Case(String label, FunctionDef fn, int input, int result, int measure) {}

    static final List<Case> CASES = List.of(
            new Case("add_with_local(5)",  addWithLocal(),   5,  10, 100_000),
            new Case("simple_loop(0)",     simpleLoop(),     0,  45, 100_000),
            new Case("fibonacci(10)",      fibonacci(),     10,  55, 100_000),
            new Case("count_primes(100)",  countPrimes(),  100,  25,  10_000)
    );

    //  Main ───────────────────────────────────────────────────────────────────

    /**
     * Usage:
     *   mvn compile exec:java -Dexec.mainClass=com.group1.interwasm.Benchmark
     *   mvn compile exec:java -Dexec.mainClass=com.group1.interwasm.Benchmark -Dexec.args="v1"
     *   mvn compile exec:java -Dexec.mainClass=com.group1.interwasm.Benchmark -Dexec.args="v2"
     *   mvn compile exec:java -Dexec.mainClass=com.group1.interwasm.Benchmark -Dexec.args="v3"
     *   mvn compile exec:java -Dexec.mainClass=com.group1.interwasm.Benchmark -Dexec.args="all"
     */
    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0].toLowerCase() : "all";
        final int WARMUP = 10_000;
        int n = CASES.size();

        boolean needV2 = !mode.equals("v1");
        boolean needV3 = mode.equals("v3") || mode.equals("all");

        System.out.println("Warming up and measuring...");
        double[] us1 = new double[n], us2 = new double[n], us3 = new double[n];
        for (int i = 0; i < n; i++) {
            Case c = CASES.get(i);
            us1[i] = timeInterpreter(c.fn(), c.input(), WARMUP, c.measure());
            if (needV2) us2[i] = timeInterpreter2(c.fn(), c.input(), WARMUP, c.measure());
            if (needV3) us3[i] = timeInterpreter3(c.fn(), c.input(), WARMUP, c.measure());
        }

        System.out.println("Running Node.js/V8 benchmark...");
        Map<String, Double> node = runNodeBenchmark();
        System.out.println();

        switch (mode) {
            case "v1"  -> printV1(us1, node);
            case "v2"  -> printV2(us1, us2, node);
            case "v3"  -> printV3(us1, us2, us3, node);
            default    -> printAll(us1, us2, us3, node);
        }
    }

    // Per-mode output 

    private static void printV1(double[] us1, Map<String, Double> node) {
        System.out.println("=== v1: naive tree-walking interpreter vs V8 JIT ===");
        System.out.println("  ControlSignal objects  |  ArrayDeque<WasmValue> stack  |  List<WasmValue> locals");
        System.out.println("  Warm-up: 10,000 calls  |  Measure: 100,000 calls\n");
        String sep = "─".repeat(68);
        System.out.println(sep);
        System.out.printf("  %-24s │ %10s │ %10s │ %8s%n", "Function", "v1 (µs)", "V8 (µs)", "gap");
        System.out.println(sep);
        for (int i = 0; i < CASES.size(); i++) {
            Case c = CASES.get(i);
            Double v8 = node.get(c.label());
            String v8s  = v8 != null ? String.format("%10.3f", v8)           : "       N/A";
            String gaps = v8 != null ? String.format("%7.1f×", us1[i] / v8)  : "     N/A";
            System.out.printf("  %-24s │ %10.3f │ %s │ %s%n",
                    c.label(), us1[i], v8s, gaps);
        }
        System.out.println(sep);
    }

    private static void printV2(double[] us1, double[] us2, Map<String, Double> node) {
        System.out.println("=== v2: optimised interpreter vs v1 vs V8 JIT ===");
        System.out.println("  int[] stack + int sp  |  int signal  |  int[] locals  |  in-place binary ops");
        System.out.println("  Warm-up: 10,000 calls  |  Measure: 100,000 calls\n");
        String sep = "─".repeat(85);
        System.out.println(sep);
        System.out.printf("  %-24s │ %10s │ %10s │ %10s │ %7s │ %7s%n",
                "Function", "v1 (µs)", "v2 (µs)", "V8 (µs)", "v1→v2", "v2→V8");
        System.out.println(sep);
        for (int i = 0; i < CASES.size(); i++) {
            Case c = CASES.get(i);
            Double v8 = node.get(c.label());
            String v8s  = v8 != null ? String.format("%10.3f", v8)           : "       N/A";
            String v2v8 = v8 != null ? String.format("%6.1f×", us2[i] / v8)  : "    N/A";
            System.out.printf("  %-24s │ %10.3f │ %10.3f │ %s │ %6.1f× │ %s%n",
                    c.label(), us1[i], us2[i], v8s,
                    us1[i] / us2[i], v2v8);
        }
        System.out.println(sep);
    }

    private static void printV3(double[] us1, double[] us2, double[] us3, Map<String, Double> node) {
        System.out.println("=== v3: flat-bytecode interpreter vs v2 vs V8 JIT ===");
        System.out.println("  int[] code compiled once  |  backpatched jumps  |  single while+switch dispatch loop");
        System.out.println("  Warm-up: 10,000 calls  |  Measure: 100,000 calls\n");
        String sep = "─".repeat(108);
        System.out.println(sep);
        System.out.printf("  %-24s │ %10s │ %10s │ %10s │ %10s │ %7s │ %7s │ %7s%n",
                "Function", "v1 (µs)", "v2 (µs)", "v3 (µs)", "V8 (µs)", "v1→v2", "v2→v3", "v3→V8");
        System.out.println(sep);
        for (int i = 0; i < CASES.size(); i++) {
            Case c = CASES.get(i);
            Double v8 = node.get(c.label());
            String v8s  = v8 != null ? String.format("%10.3f", v8)           : "       N/A";
            String v3v8 = v8 != null ? String.format("%6.1f×", us3[i] / v8)  : "    N/A";
            System.out.printf("  %-24s │ %10.3f │ %10.3f │ %10.3f │ %s │ %6.1f× │ %6.1f× │ %s%n",
                    c.label(), us1[i], us2[i], us3[i], v8s,
                    us1[i] / us2[i], us2[i] / us3[i], v3v8);
        }
        System.out.println(sep);
    }

    private static void printAll(double[] us1, double[] us2, double[] us3, Map<String, Double> node) {
        System.out.println("=== Full comparison: v1 vs v2 vs v3 vs V8 JIT ===");
        System.out.println("  Warm-up: 10,000 calls  |  Measure: 100,000 calls\n");
        String sep = "─".repeat(108);
        System.out.println(sep);
        System.out.printf("  %-24s │ %10s │ %10s │ %10s │ %10s │ %7s │ %7s │ %7s%n",
                "Function", "v1 (µs)", "v2 (µs)", "v3 (µs)", "V8 (µs)", "v1→v2", "v2→v3", "v3→V8");
        System.out.println(sep);
        for (int i = 0; i < CASES.size(); i++) {
            Case c = CASES.get(i);
            Double v8 = node.get(c.label());
            String v8s  = v8 != null ? String.format("%10.3f", v8)           : "       N/A";
            String v3v8 = v8 != null ? String.format("%6.1f×", us3[i] / v8)  : "    N/A";
            System.out.printf("  %-24s │ %10.3f │ %10.3f │ %10.3f │ %s │ %6.1f× │ %6.1f× │ %s%n",
                    c.label(), us1[i], us2[i], us3[i], v8s,
                    us1[i] / us2[i], us2[i] / us3[i], v3v8);
        }
        System.out.println(sep);
    }
}
