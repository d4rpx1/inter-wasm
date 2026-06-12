package com.group4.interwasm.bytecode;

/**
 * The size of a section within the wasm file is encoding using Leb128
 * there, the least significant bit is not used for the number,
 * it is instead used to communicate whether there is one more byte left
 * when it's 1 it means there's one more byte to follow when it's 0 it means
 * it's the last byte
 * e.x. 00000001 00000000
 */
public class Leb128 {
        private Leb128() {
        }

        public static Result readUnsigned(byte[] bytes, int offset) {
            int result = 0;
            int shift = 0;
            int position = offset;

            while (true) {
                if (position >= bytes.length) {
                    throw new IllegalArgumentException("Unexpected end of input while reading ULEB128");
                }

                int b = bytes[position] & 0xFF;
                position++;

                result |= (b & 0x7F) << shift;

                if ((b & 0x80) == 0) {
                    return new Result(result, position - offset);
                }

                shift += 7;

                if (shift >= 32) {
                    throw new IllegalArgumentException("ULEB128 value is too large for int");
                }
            }
        }

        public record Result(int value, int bytesRead) {
        }
}
