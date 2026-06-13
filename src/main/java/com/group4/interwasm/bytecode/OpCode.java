package com.group4.interwasm.bytecode;


public enum OpCode {
    END(0x0B),
    LOCAL_GET(0x20),
    LOCAL_SET(0x21),
    I32_CONST(0x41),
    I32_ADD(0x6A);

    private final int bytecode;

    OpCode(int bytecode) {
        this.bytecode = bytecode;
    }

    public int bytecode() {
        return bytecode;
    }

    public static OpCode fromBytecode(int bytecode) {
        return switch (bytecode) {
            case 0x0B -> END;
            case 0x20 -> LOCAL_GET;
            case 0x21 -> LOCAL_SET;
            case 0x41 -> I32_CONST;
            case 0x6A -> I32_ADD;
            default -> throw new IllegalArgumentException(
                    "Unsupported opcode: 0x" + Integer.toHexString(bytecode)
            );
        };
    }
}