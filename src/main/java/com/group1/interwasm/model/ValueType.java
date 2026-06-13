package com.group1.interwasm.model;

public enum ValueType {
    I32;

    /**
     * Convert a binary code (bytecode) code of the wasm file to a ValueType
     * @param code the binary code to convert
     * @return the value type
     */
    public static ValueType fromBinaryCode(int code) {
        return switch (code) {
            case 0x7F -> I32;
            default -> throw new IllegalArgumentException(
                    "Unsupported value type: 0x" + Integer.toHexString(code)
            );
        };
    }
}