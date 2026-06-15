package com.group1.interwasm.bytecode;

import com.group1.interwasm.instruction.arithmetic.I32Mul;
import com.group1.interwasm.instruction.arithmetic.I32RemS;
import com.group1.interwasm.instruction.comparisons.I32Eqz;
import com.group1.interwasm.instruction.comparisons.I32GeS;
import com.group1.interwasm.instruction.comparisons.I32GtS;
import com.group1.interwasm.instruction.control_flow.*;
import com.group1.interwasm.instruction.i32.ConstI32;
import com.group1.interwasm.instruction.arithmetic.I32Add;
import com.group1.interwasm.instruction.locals.LocalGet;
import com.group1.interwasm.instruction.locals.LocalSet;
import com.group1.interwasm.model.FunctionDef;
import com.group1.interwasm.model.ValueType;
import com.group1.interwasm.util.FilesUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class WasmReaderTest {
    @Test
    void readAddGeneratedByClangTest() {
        String wasmFilePath = FilesUtil.getPathToResourceFile("wasm-examples/add/add.wasm");
        AtomicReference<FunctionDef> functionDefAtomic = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmReader reader = new WasmReader(new File(wasmFilePath));
            functionDefAtomic.set(reader.readFunctionDefinition());
        });

        FunctionDef functionDef = functionDefAtomic.get();

        assertEquals(ValueType.I32, functionDef.type().resultType()); // return type is integer
        assertEquals(List.of(ValueType.I32), functionDef.type().params()); // one int parameter
        assertEquals(List.of(), functionDef.locals()); // no locals, because they get optimized away by clang

        assertEquals(
                List.of(
                        new LocalGet(0),
                        new ConstI32(10),
                        new I32Add()
                ), functionDef.body()
        );
    }

    @Test
    void readAddWithLocalTest() {
        String wasmFilePath = FilesUtil.getPathToResourceFile("wasm-examples/add_with_local/add_with_local.wasm");
        AtomicReference<FunctionDef> functionDefAtomic = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmReader reader = new WasmReader(new File(wasmFilePath));
            functionDefAtomic.set(reader.readFunctionDefinition());
        });

        FunctionDef functionDef = functionDefAtomic.get();

        assertEquals(ValueType.I32, functionDef.type().resultType()); // return type is integer
        assertEquals(List.of(ValueType.I32), functionDef.type().params()); // one integer parameter parameters (a from add.wasm)
        assertEquals(List.of(ValueType.I32), functionDef.locals()); // one integer local (b from add.wasm)

        assertEquals(
                List.of(
                        new ConstI32(5),
                        new LocalSet(1),
                        new LocalGet(0),
                        new LocalGet(1),
                        new I32Add()
                ), functionDef.body()
        );
    }

    @Test
    void readCountPrimes() {
        String wasmFilePath = FilesUtil.getPathToResourceFile("wasm-examples/count_primes/count_primes.wasm");
        AtomicReference<FunctionDef> functionDefAtomic = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmReader reader = new WasmReader(new File(wasmFilePath));
            functionDefAtomic.set(reader.readFunctionDefinition());
        });

        FunctionDef functionDef = functionDefAtomic.get();

        assertEquals(ValueType.I32, functionDef.type().resultType()); // return type is integer
        assertEquals(List.of(ValueType.I32), functionDef.type().params()); // parameter $n (Index 0)

        // Wir haben 4 lokale Variablen: $i, $count, $d, $is_prime (Indizes 1 bis 4)
        assertEquals(List.of(
                ValueType.I32,
                ValueType.I32,
                ValueType.I32,
                ValueType.I32
        ), functionDef.locals());

        // Der AST für die Primzahlberechnung
        assertEquals(
                List.of(
                        // i = 2
                        new ConstI32(2),
                        new LocalSet(1),

                        // count = 0
                        new ConstI32(0),
                        new LocalSet(2),

                        new Block(
                                List.of(
                                        new Loop(
                                                List.of(
                                                        // if i > n, stop
                                                        new LocalGet(1), // $i
                                                        new LocalGet(0), // $n
                                                        new I32GtS(),
                                                        new BrIf(1),     // bricht $done ab (Tiefe 1)

                                                        // is_prime = 1
                                                        new ConstI32(1),
                                                        new LocalSet(4),

                                                        // d = 2
                                                        new ConstI32(2),
                                                        new LocalSet(3),

                                                        new Block(
                                                                List.of(
                                                                        new Loop(
                                                                                List.of(
                                                                                        // if d * d > i, stop checking
                                                                                        new LocalGet(3), // $d
                                                                                        new LocalGet(3), // $d
                                                                                        new I32Mul(),
                                                                                        new LocalGet(1), // $i
                                                                                        new I32GtS(),
                                                                                        new BrIf(1),     // bricht $check_done ab (Tiefe 1)

                                                                                        // if i % d == 0
                                                                                        new LocalGet(1), // $i
                                                                                        new LocalGet(3), // $d
                                                                                        new I32RemS(),
                                                                                        new I32Eqz(),
                                                                                        new If(
                                                                                                // Then-Zweig
                                                                                                List.of(
                                                                                                        new ConstI32(0),
                                                                                                        new LocalSet(4), // is_prime = 0
                                                                                                        new Br(2)        // br $check_done (überspringt if, inner_loop und beendet check_done)
                                                                                                ),
                                                                                                // Else-Zweig (leer)
                                                                                                List.of()
                                                                                        ),

                                                                                        // d = d + 1
                                                                                        new LocalGet(3), // $d
                                                                                        new ConstI32(1),
                                                                                        new I32Add(),
                                                                                        new LocalSet(3), // $d

                                                                                        // continue inner loop
                                                                                        new Br(0)
                                                                                )
                                                                        )
                                                                )
                                                        ),

                                                        // count = count + is_prime
                                                        new LocalGet(2), // $count
                                                        new LocalGet(4), // $is_prime
                                                        new I32Add(),
                                                        new LocalSet(2), // $count

                                                        // i = i + 1
                                                        new LocalGet(1), // $i
                                                        new ConstI32(1),
                                                        new I32Add(),
                                                        new LocalSet(1), // $i

                                                        // continue outer loop
                                                        new Br(0)
                                                )
                                        )
                                )
                        ),

                        // return count
                        new LocalGet(2)
                ), functionDef.body()
        );
    }

    @Test
    void readFibonacci() {
        String wasmFilePath = FilesUtil.getPathToResourceFile("wasm-examples/fibonacci/fibonacci.wasm");
        AtomicReference<FunctionDef> functionDefAtomic = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmReader reader = new WasmReader(new File(wasmFilePath));
            functionDefAtomic.set(reader.readFunctionDefinition());
        });

        FunctionDef functionDef = functionDefAtomic.get();

        assertEquals(ValueType.I32, functionDef.type().resultType()); // return type is integer
        assertEquals(List.of(ValueType.I32), functionDef.type().params()); // parameter $n (Index 0)

        // Wir haben 4 lokale Variablen: $a, $b, $i, $tmp (Indizes 1 bis 4)
        assertEquals(List.of(
                ValueType.I32,
                ValueType.I32,
                ValueType.I32,
                ValueType.I32
        ), functionDef.locals());

        // Der AST für die Fibonacci-Berechnung
        assertEquals(
                List.of(
                        // a = 0
                        new ConstI32(0),
                        new LocalSet(1),

                        // b = 1
                        new ConstI32(1),
                        new LocalSet(2),

                        // i = 0
                        new ConstI32(0),
                        new LocalSet(3),

                        new Block(
                                List.of(
                                        new Loop(
                                                List.of(
                                                        // if i >= n, break
                                                        new LocalGet(3), // $i
                                                        new LocalGet(0), // $n
                                                        new I32GeS(),
                                                        new BrIf(1),     // Springt aus dem Block (break)

                                                        // tmp = a + b
                                                        new LocalGet(1), // $a
                                                        new LocalGet(2), // $b
                                                        new I32Add(),
                                                        new LocalSet(4), // $tmp

                                                        // a = b
                                                        new LocalGet(2), // $b
                                                        new LocalSet(1), // $a

                                                        // b = tmp
                                                        new LocalGet(4), // $tmp
                                                        new LocalSet(2), // $b

                                                        // i = i + 1
                                                        new LocalGet(3), // $i
                                                        new ConstI32(1),
                                                        new I32Add(),
                                                        new LocalSet(3), // $i

                                                        // continue loop
                                                        new Br(0)
                                                )
                                        )
                                )
                        ),

                        // return a
                        new LocalGet(1)
                ), functionDef.body()
        );
    }

    @Test
    void readSimpleLoop() {
        String wasmFilePath = FilesUtil.getPathToResourceFile("wasm-examples/simple_loop/simple_loop.wasm");
        AtomicReference<FunctionDef> functionDefAtomic = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            WasmReader reader = new WasmReader(new File(wasmFilePath));
            functionDefAtomic.set(reader.readFunctionDefinition());
        });

        FunctionDef functionDef = functionDefAtomic.get();

        assertEquals(ValueType.I32, functionDef.type().resultType()); // return type is integer
        assertEquals(List.of(ValueType.I32), functionDef.type().params()); // one integer parameter parameters (a from add.wasm)
        assertEquals(List.of(ValueType.I32), functionDef.locals()); // one integer local (b from add.wasm)

        assertEquals(
                List.of(
                        new ConstI32(0),
                        new LocalSet(1),
                        new Block(
                                List.of(
                                        new Loop(
                                                List.of(
                                                        new LocalGet(1),
                                                        new ConstI32(10),
                                                        new I32GeS(),
                                                        new BrIf(1),

                                                        new LocalGet(0),
                                                        new LocalGet(1),
                                                        new I32Add(),
                                                        new LocalSet(0),

                                                        new LocalGet(1),
                                                        new ConstI32(1),
                                                        new I32Add(),
                                                        new LocalSet(1),

                                                        new Br(0)
                                                )
                                        )
                                )
                        ),
                        new LocalGet(0)         // return x
                ), functionDef.body()
        );
    }
}