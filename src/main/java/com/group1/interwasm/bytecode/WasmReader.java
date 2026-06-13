package com.group1.interwasm.bytecode;

import com.group1.interwasm.instruction.i32.ConstI32;
import com.group1.interwasm.instruction.arithmetic.I32Add;
import com.group1.interwasm.instruction.locals.LocalGet;
import com.group1.interwasm.instruction.locals.LocalSet;
import com.group1.interwasm.model.FunctionDef;
import com.group1.interwasm.model.FunctionType;
import com.group1.interwasm.model.Instruction;
import com.group1.interwasm.model.ValueType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Reads .wasm file with one single function and creates FunctionDef
 */
public class WasmReader {
    private File bytecodeFile;
    private byte[] bytecode;

    // https://charlycst.github.io/posts/wasm-encoding/
    private static final byte[] MAGIC_BYTES =  HexFormat.of().parseHex("0061736D01000000");

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
        // The code section and type section are bound by order
        // so e.x. type 0 is the function type that belongs to the instructions
        // from function body 0 of the code section
        // we also skip the size of the code section here (first byte) because we
        // only interpret wasm files that have one single function
        int actByte = 1;
        actByte += 1; // also skip the size of the body with ID 0 (function body)
        int localDeclarationGroupsSize = bytes[actByte++];

        if (localDeclarationGroupsSize > 1) {
            throw new IllegalArgumentException("Multiple local declaration groups not supported! Only I32 values are supported.");
        }
        else if (localDeclarationGroupsSize == 0) { // no local declaration groups
            return List.of();
        }

        int localsSize = bytes[actByte++]; // TODO maybe use the Le128 here but we won't have more than 128 locals anyways

        List<ValueType> locals = new ArrayList<>();

        for (int i = 0; i < localsSize; i++) {
            locals.add(ValueType.fromBinaryCode(bytes[actByte]));
        }

        return locals;
    }

    /**
     * Extracts instructions from code section
     *
     * @param bytes raw bytes of the code section of the wasm binary
     * @return a list of the instructions
     */
    private List<Instruction> extractInstructions(byte[] bytes) {
        // The code section and type section are bound by order
        // so e.x. type 0 is the function type that belongs to the instructions
        // from function body 0 of the code section
        int actByte = 1; // skip the size of the code section here (first byte) because we
        // only interpret wasm files that have one single function
        actByte += 1; // also skip the size of the body with ID 0 (function body)
        int localDeclarationGroupsSize = bytes[actByte++];

        actByte += localDeclarationGroupsSize * 2; //  skip the locals since we read them in extractLocals
        // the locals are stored in groups like 02 127 would be first group that has two integer locals
        // then the second group would be 01 xxx that has one value type xxx so we skip by multiplying local
        // declaration groups by two

        // read opcodes
        List<Instruction> instructions = new ArrayList<>();

        while (true) {
            if (actByte >= bytes.length) {
                throw new IllegalArgumentException("Reached end of instruction bytecode and no 0x0B instruction" +
                        " found, which should signal the end of the bytecode.");
            }

            OpCode opcode = OpCode.fromBytecode(bytes[actByte++]);

            switch (opcode) {
                case END -> {
                    return instructions;
                }

                case I32_CONST  -> {
                    var result = Leb128.readUnsigned(bytes, actByte);
                    actByte += result.bytesRead();
                    int value = result.value();

                    instructions.add(new ConstI32(value));
                }

                case LOCAL_GET  -> {
                    var result = Leb128.readUnsigned(bytes, actByte);
                    actByte += result.bytesRead();
                    int index = result.value();

                    instructions.add(new LocalGet(index));
                }

                case LOCAL_SET  -> {
                    var result = Leb128.readUnsigned(bytes, actByte);
                    actByte += result.bytesRead();
                    int index = result.value();

                    instructions.add(new LocalSet(index));
                }

                // TODO: add other opcodes

                case I32_ADD -> {
                    instructions.add(new I32Add());
                }
            }
        }
    }

    /**
     * Extracts function type from type section e.x. (i32, i32) -> i32
     * @param bytes the raw bytes of the type section from the wasm file
     * @return object of FunctionType
     */
    private FunctionType extractFunctionType(byte[] bytes) {
        // The type section starts with the size of the types
        // we only support one function per file, also one type
        // then, comes 0x60 for the function kind.
        // then we have a Leb128-encoded size of params
        // after which the params are followed and the size of return values
        // however our interpreter only supports one return value
        int actByte = 0;

        int amountTypes = bytes[actByte++];

        if (amountTypes > 1) {
            throw new IllegalArgumentException("This interpreter only supports .wasm files with one single function!");
        }

        int kind = bytes[actByte++];

        if (kind != 0x60) {
            throw new IllegalArgumentException("Expected function kind 0x60 but got 0x" + Integer.toHexString(kind));
        }

        // Get all parameters

        var paramCountResult = Leb128.readUnsigned(bytes, actByte);
        actByte = paramCountResult.bytesRead() + actByte;
        int paramCount = paramCountResult.value();

        List<ValueType> params = new ArrayList<>();

        int parametersSize = paramCount + actByte;
        while (actByte < parametersSize) {
            params.add(ValueType.fromBinaryCode(bytes[actByte]));
            actByte += 1;
        }

        // Get return value
        actByte += 1; // skip size of the return value

        ValueType returnValue = ValueType.fromBinaryCode(bytes[actByte]);

        return new FunctionType(
                params,
                returnValue
        );
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
