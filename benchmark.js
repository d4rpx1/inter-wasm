/**
 * Node.js / V8 JIT WebAssembly benchmark.
 * Measures wall-clock time for our four .wasm examples.
 *
 * Run from the project root:
 *   node benchmark.js
 *
 * V8 JIT-compiles WebAssembly to native machine code, so this is essentially
 * measuring pure Nutzlast -> the actual computation with no interpreter overhead.
 * Compare the output against the Java interpreter benchmark to see the total
 * cost of Dispatch + Operandenzugriff in our tree-walking interpreter.
 */

const fs = require('fs');
const { performance } = require('perf_hooks');

const BASE = 'src/main/resources/wasm-examples';

async function loadWasm(path) {
    const bytes = fs.readFileSync(path);
    const { instance } = await WebAssembly.instantiate(bytes);
    return instance.exports._start;
}

function bench(label, fn, input, warmupReps, measureReps) {
    // Warm up — lets V8 JIT compile the function
    for (let i = 0; i < warmupReps; i++) fn(input);

    const t0 = performance.now();
    for (let i = 0; i < measureReps; i++) fn(input);
    const ms = performance.now() - t0;

    const usPerCall = (ms / measureReps) * 1000;
    const callsPerSec = (measureReps / ms) * 1000;
    console.log(
        `${label.padEnd(30)} input=${String(input).padStart(5)}` +
        `  result=${fn(input)}` +
        `  ${usPerCall.toFixed(3).padStart(8)} µs/call` +
        `  ${Math.round(callsPerSec).toLocaleString().padStart(12)} calls/s`
    );
}

async function main() {
    console.log('=== Node.js / V8 JIT WebAssembly benchmark ===\n');

    const addWithLocal = await loadWasm(`${BASE}/add_with_local/add_with_local.wasm`);
    const simpleLoop   = await loadWasm(`${BASE}/simple_loop/simple_loop.wasm`);
    const fibonacci    = await loadWasm(`${BASE}/fibonacci/fibonacci.wasm`);
    const countPrimes  = await loadWasm(`${BASE}/count_primes/count_primes.wasm`);

    const WARMUP   = 10_000;
    const MEASURE  = 100_000;

    bench('add_with_local(5)',   addWithLocal, 5,   WARMUP, MEASURE);
    bench('simple_loop(0)',      simpleLoop,   0,   WARMUP, MEASURE);
    bench('fibonacci(10)',       fibonacci,    10,  WARMUP, MEASURE);
    bench('count_primes(100)',   countPrimes,  100, WARMUP, 10_000);

    console.log('\nNote: V8 JIT ≈ pure Nutzlast (no interpreter dispatch/operand overhead).');
    console.log('Compare these numbers against the Java interpreter benchmark.');
}

main().catch(console.error);
