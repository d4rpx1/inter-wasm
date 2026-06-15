package com.group1.interwasm.bytecode;


public enum OpCode {
    END(0x0B),
    LOCAL_GET(0x20),
    LOCAL_SET(0x21),
    I32_CONST(0x41),
    I32_ADD(0x6A),
    BLOCK(0x02),
    LOOP(0x03),
    BR(0x0C),
    BR_IF(0x0D),
    RETURN(0x0F),
    LOCAL_TEE(0x22),
    I32_EQZ(0x45),
    I32_LT_S(0x48),
    I32_LE_S(0x4C),
    I32_SUB(0x6B),
    I32_MUL(0x6C),
    I32_REM_S(0x6F),
    IF(0x04),
    ELSE(0x05),
    I32_EQ(0x46),
    I32_NE(0x47),
    I32_GT_S(0x4A),
    I32_GE_S(0x4E),
    I32_DIV_S(0x6D);

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
            case 0x02 -> BLOCK;
            case 0x03 -> LOOP;
            case 0x0C -> BR;
            case 0x0D -> BR_IF;
            case 0x0F -> RETURN;
            case 0x22 -> LOCAL_TEE;
            case 0x45 -> I32_EQZ;
            case 0x48 -> I32_LT_S;
            case 0x4C -> I32_LE_S;
            case 0x6B -> I32_SUB;
            case 0x6C -> I32_MUL;
            case 0x6F -> I32_REM_S;
            case 0x04 -> IF;
            case 0x05 -> ELSE;
            case 0x46 -> I32_EQ;
            case 0x47 -> I32_NE;
            case 0x4A -> I32_GT_S;
            case 0x4E -> I32_GE_S;
            case 0x6D -> I32_DIV_S;
            default -> throw new IllegalArgumentException(
                    "Unsupported opcode: 0x" + Integer.toHexString(bytecode)
            );
        };
    }
}