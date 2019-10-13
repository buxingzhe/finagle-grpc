package io.protostuff;

import io.netty.buffer.ByteBuf;

import static io.protostuff.StringSerializer.*;

abstract class ByteBufProtobufUtils {

    public static void writeUTF8VarDelimited(CharSequence str, ByteBuf byteBuf) {
        int len = str.length();
        if (len == 0) {
            byteBuf.writeByte(0);
            return;
        }

        if (len < ONE_BYTE_EXCLUSIVE) {
            // the varint will be max 1-byte. (even if all chars are non-ascii)
            int startIndex = byteBuf.writerIndex();
            byteBuf.writeByte(0);
            writeUTF8(str, len, byteBuf);
            int endIndex = byteBuf.writerIndex();
            int size = endIndex - startIndex - 1;
            byteBuf.writerIndex(startIndex);
            byteBuf.writeByte(size);
            byteBuf.writerIndex(endIndex);
        } else if (len < TWO_BYTE_EXCLUSIVE) {
            // the varint will be max 2-bytes and could be 1-byte. (even if all non-ascii)
            writeUTF8VarDelimited(str, len, TWO_BYTE_LOWER_LIMIT, 2, byteBuf);
        } else if (len < THREE_BYTE_EXCLUSIVE) {
            // the varint will be max 3-bytes and could be 2-bytes. (even if all non-ascii)
            writeUTF8VarDelimited(str, len, THREE_BYTE_LOWER_LIMIT, 3, byteBuf);
        } else if (len < FOUR_BYTE_EXCLUSIVE) {
            // the varint will be max 4-bytes and could be 3-bytes. (even if all non-ascii)
            writeUTF8VarDelimited(str, len, FOUR_BYTE_LOWER_LIMIT, 4, byteBuf);
        } else {
            // the varint will be max 5-bytes and could be 4-bytes. (even if all non-ascii)
            writeUTF8VarDelimited(str, len, FIVE_BYTE_LOWER_LIMIT, 5, byteBuf);
        }
    }

    private static void writeUTF8VarDelimited(CharSequence str, int len, int lowerLimit, int expectedSize, ByteBuf byteBuf) {
        int beginIndex = byteBuf.writerIndex();
        int startWriteIndex = beginIndex + expectedSize;
        byteBuf.writerIndex(startWriteIndex);
        writeUTF8(str, len, byteBuf);
        int endIndex = byteBuf.writerIndex();
        int size = endIndex - startWriteIndex;
        int expectedSizeVar = expectedSize;
        if (size < lowerLimit) {
            ByteBuf dup = byteBuf.slice(startWriteIndex, size);
            byteBuf.writerIndex(startWriteIndex - 1);
            byteBuf.writeBytes(dup);
            expectedSizeVar = expectedSizeVar - 1;
            endIndex = endIndex - 1;
        }

        byteBuf.writerIndex(beginIndex);
        while (--expectedSizeVar > 0) {
            byteBuf.writeByte((size & 0x7F) | 0x80);
            size >>>= 7;
        }
        byteBuf.writeByte(size).writerIndex(endIndex);
    }

    private static void writeUTF8(CharSequence str, int len, ByteBuf byteBuf) {
        int c = 0;
        int i = 0;
        while(true) {
            while(i != len && (c = str.charAt(i++)) < 0x0080) {
                byteBuf.writeByte(c);
            }

            if (i == len && c < 0x0080) {
                return;
            }

            if (i < len && Character.isHighSurrogate((char)c) && Character.isLowSurrogate(str.charAt(i))) {
                int codePoint = Character.toCodePoint((char)c, str.charAt(i));
                byteBuf.writeByte(0xF0 | ((codePoint >> 18) & 0x07));
                byteBuf.writeByte(0x80 | ((codePoint >> 12) & 0x3F));
                byteBuf.writeByte(0x80 | ((codePoint >> 6) & 0x3F));
                byteBuf.writeByte(0x80 | (codePoint & 0x3F));
                i += 1;
            } else {
                byteBuf.writeByte(0xE0 | ((c >> 12) & 0x0F));
                byteBuf.writeByte(0x80 | ((c >> 6) & 0x3F));
                byteBuf.writeByte(0x80 | (c & 0x3F));
            }
        }
    }
}
