package com.group4.interwasm.bytecode;

import com.group4.interwasm.model.FunctionDef;
import com.group4.interwasm.model.FunctionType;
import com.group4.interwasm.model.Instruction;
import com.group4.interwasm.model.ValueType;
import jdk.jshell.spi.ExecutionControl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;

/**
 * Reads .wasm file with one single function and creates FunctionDef
 */
public class WasmReader {
    private File bytecodeFile;
    private byte[] bytecode;

    // https://charlycst.github.io/posts/wasm-encoding/
    private static byte[] MAGIC_BYTES =  HexFormat.of().parseHex("0061736D01000000");

    public WasmReader(File bytecodeFile) {
        this.bytecodeFile = bytecodeFile;
    }

    /**
     * Reads .wasm file that contains one single function and creates FunctionDef
     *
     * @return FunctionDef (definition of function)
     * @throws IOException when file cannot be read
     */
    public FunctionDef readFunctionDefinition() throws IOException {
        this.bytecode = Files.readAllBytes(bytecodeFile.toPath());

        if(!hasMagicBytes()) {
            throw new IllegalArgumentException("File " + bytecodeFile.getName() + " does not have the wasm magic bytes.");
        }

        HashMap<Integer, byte[]> sections = readWasmSections();

        FunctionType functionType = extractFunctionType(sections.get(WasmSection.TYPE_SECTION.getId()));
        List<ValueType> locals = extractLocals(sections.get(WasmSection.CODE_SECTION.getId()));
        List<Instruction> instructions = extractInstructions(sections.get(WasmSection.CODE_SECTION.getId()));

        return new FunctionDef(
                functionType,
                locals,
                instructions
        );
    }

    /**
     * Extracts locals from code section
     *
     * @param bytes raw bytes of the code section of the wasm binary
     * @return list of value types of locals
     */
    private List<ValueType> extractLocals(byte[] bytes) {
        throw new IllegalArgumentException("Not yet implemented");
    }

    /**
     * Extracts instructions from code section
     *
     * @param bytes raw bytes of the code section of the wasm binary
     * @return a list of the instructions
     */
    private List<Instruction> extractInstructions(byte[] bytes) {
        throw new IllegalArgumentException("Not yet implemented");
    }

    /**
     * Extracts function type from type section e.x. (i32, i32) -> i32
     * @param bytes the raw bytes of the type section from the wasm file
     * @return object of FunctionType
     */
    private FunctionType extractFunctionType(byte[] bytes) {
        throw new IllegalArgumentException("Not yet implemented");
    }

    /**
     * Reads the sections of a wasm file
     * @return a hashmap with section id as key and bytes as value
     */
    private HashMap<Integer, byte[]> readWasmSections() {
        HashMap<Integer, byte[]> sections = new HashMap<>();

        int actByte = 8; // skip magic number, since it has 8 bytes

        while (actByte < this.bytecode.length) {
            int sectionId = this.bytecode[actByte++];

            var sizeResult = Leb128.readUnsigned(this.bytecode, actByte);
            actByte += sizeResult.bytesRead();

            int sectionSize = sizeResult.value();

            sections.put(sectionId, Arrays.copyOfRange(this.bytecode, actByte, actByte+sectionSize));

            actByte += sectionSize;
        }

        return sections;
    }

    /**
     * Verify if magic bytes match the wasm magic bytes
     *
     * @return true if the magic bytes match
     */
    private boolean hasMagicBytes() {
        for (int i = 0; i < MAGIC_BYTES.length; i++) {
            if(this.bytecode[i] != MAGIC_BYTES[i]) {
                return false;
            }
        }

        return true;
    }
}
