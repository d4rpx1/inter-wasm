# InterWasm

InterWasm is an interpreter-engine for a Subset of WebAssembly (WASM) bytecode. We use pre-built WASM-Bytecode using `emcc` from https://github.com/emscripten-core/emsdk and write an interpreter for the bytecode. We benchmark our interpreter-engine against real Reference-Engines (wasm3, wuasmtime). 

## Subset Scope

Only `i32`. This is enough for all interesting benchmarks.

- **Numeric operations:** `i32.const`, `add/sub/mul/div_s/rem_s`, comparisons, `eqz`
- **Locals/Globals:** `local.get/set/tee`, `global.get/set`
- **Control flow:** `block`, `loop`, `if/else`, `br`, `br_if`, `return`, `call`
- **Memory:** `i32.load/store`, `memory.size/grow` linear memory as `uint8_t[]`
- **Omitted:** floats, `call_indirect`/tables, SIMD, everything post-MVP

## Helpers

* wasm2wat: converts from binary wasm to a text format for debugging (https://webassembly.github.io/wabt/demo/wasm2wat/)
* generating the wasm bytecode: emcc <filename.c>