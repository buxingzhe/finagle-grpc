package io.protostuff;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.ByteBuffer;

import static io.protostuff.WireFormat.*;

public class ByteBufProtobufOutput implements Output {
    //private static final int LITTLE_ENDIAN_32_SIZE = 4;
    //private static final int LITTLE_ENDIAN_64_SIZE = 8;
    private ByteBuf buffer;

    public ByteBufProtobufOutput(ByteBuf buffer) {
        this.buffer = buffer;
    }

    public void writeUInt64(int fieldNumber, long value, boolean repeated) throws IOException{
        writeTagAndRawVarInt64(makeTag(fieldNumber, WIRETYPE_VARINT), value);
    }


    public void writeFloat(int fieldNumber, float value, boolean repeated) throws IOException{
        writeTagAndRawLittleEndian32(makeTag(fieldNumber, WIRETYPE_FIXED32), Float.floatToRawIntBits(value));
    }


    public void writeBool(int fieldNumber, boolean value, boolean repeated) throws IOException{
        writeTagAndRawVarInt32(makeTag(fieldNumber, WIRETYPE_VARINT), value ? 1 : 0);
    }


    public void writeInt64(int fieldNumber, long value, boolean repeated) throws IOException{
        writeTagAndRawVarInt64(makeTag(fieldNumber, WIRETYPE_VARINT), value);
    }


    public void writeSInt32(int fieldNumber, int value, boolean repeated) throws IOException{
        writeTagAndRawVarInt32(makeTag(fieldNumber, WIRETYPE_VARINT), encodeZigZag32(value));
    }


    public void writeSFixed32(int fieldNumber, int value, boolean repeated) throws IOException{
        writeTagAndRawLittleEndian32(makeTag(fieldNumber, WIRETYPE_FIXED32), value);
    }


    public void writeFixed64(int fieldNumber, long value, boolean repeated) throws IOException{
        writeTagAndRawLittleEndian64(makeTag(fieldNumber, WIRETYPE_FIXED64), value);
    }


    public void writeDouble(int fieldNumber, double value, boolean repeated) throws IOException{
        writeTagAndRawLittleEndian64(makeTag(fieldNumber, WIRETYPE_FIXED64), Double.doubleToRawLongBits(value));
    }


    public void writeByteArray(int fieldNumber, byte[] value, boolean repeated) throws IOException{
        writeTagAndByteArray(makeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED), value, 0, value.length);
    }


    public void writeEnum(int fieldNumber, int value, boolean repeated) throws IOException{
        writeInt32(fieldNumber, value, repeated);
    }


    public <T> void writeObject(int fieldNumber, T value, Schema<T> schema, boolean repeated) throws IOException{
        if (fieldNumber < 16) {
            buffer.writeByte(makeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED));
        } else {
            writeRawVarInt32(makeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED));
        }

        int startWriterIndex = buffer.writerIndex();
        int bodyStartIndex = startWriterIndex + 5;
        buffer.writerIndex(bodyStartIndex);
        schema.writeTo(this, value);
        int endWriterIndex = buffer.writerIndex();

        int msgSize = endWriterIndex - bodyStartIndex;
        buffer.writerIndex(startWriterIndex);
        writeRawVarInt32(msgSize);
        ByteBuf body = buffer.slice(bodyStartIndex, msgSize);
        buffer.writeBytes(body);
    }


    public void writeByteRange(boolean utf8String, int fieldNumber, byte[] value, int offset, int length, boolean repeated) throws IOException{
        writeTagAndByteArray(makeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED), value, offset, length);
    }


    public void writeUInt32(int fieldNumber, int value, boolean repeated) throws IOException{
        writeTagAndRawVarInt32(makeTag(fieldNumber, WIRETYPE_VARINT), value);
    }


    public void writeInt32(int fieldNumber, int value, boolean repeated) throws IOException{
        if (value < 0) writeTagAndRawVarInt64(makeTag(fieldNumber, WIRETYPE_VARINT), value);
        else writeTagAndRawVarInt32(makeTag(fieldNumber, WIRETYPE_VARINT), value);
    }


    public void writeBytes(int fieldNumber, ByteString value, boolean repeated) throws IOException{
        writeByteArray(fieldNumber, value.getBytes(), repeated);
    }

    public void writeBytes(int fieldNumber, ByteBuffer value, boolean repeated) throws IOException{
        writeByteRange(false, fieldNumber, value.array(), value.arrayOffset() + value.position(), value.remaining(), repeated);
    }


    public void writeSInt64(int fieldNumber, long value, boolean repeated) throws IOException{
        writeTagAndRawVarInt64(makeTag(fieldNumber, WIRETYPE_VARINT), encodeZigZag64(value));
    }


    public void writeSFixed64(int fieldNumber, long value, boolean repeated) throws IOException{
        writeTagAndRawLittleEndian64(makeTag(fieldNumber, WIRETYPE_FIXED64), value);
    }


    public void writeFixed32(int fieldNumber, int value, boolean repeated) throws IOException{
        writeTagAndRawLittleEndian32(makeTag(fieldNumber, WIRETYPE_FIXED32), value);
    }


    public void writeString(int fieldNumber, CharSequence value, boolean repeated) throws IOException{
        writeRawVarInt32(makeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED));
        ByteBufProtobufUtils.writeUTF8VarDelimited(value, buffer);
    }


    private void writeTagAndByteArray(int tag, byte[] array, int offset, int length) {
        writeTagAndRawVarInt32(tag, length);
        if (length > 0) {
            buffer.writeBytes(array, offset, length);
        }
    }


    private void writeTagAndRawVarInt32(int tag, int value) {
        writeRawVarInt32(tag);
        writeRawVarInt32(value);
    }


    private void writeTagAndRawLittleEndian32(int tag, int value) {
        writeRawVarInt32(tag);
        writeRawLittleEndian32(value);
    }


    private void writeTagAndRawVarInt64(int tag, long value) {
        writeRawVarInt32(tag);
        writeRawVarInt64(value);
    }


    private void writeTagAndRawLittleEndian64(int tag, long value) {
        writeRawVarInt32(tag);
        writeRawLittleEndian64(value);
    }


    private void writeRawVarInt32(int value) {
        int size = computeRawVarint32Size(value);
        if (size == 1) {
            buffer.writeByte(value);
        } else {
            int last = size - 1;
            int i = 0;
            int nextValue = value;
            while (i < last) {
                buffer.writeByte((nextValue & 0x7F) | 0x80);
                i += 1;
                nextValue >>>= 7;
            }
            buffer.writeByte(nextValue);
        }
    }


    private void writeRawVarInt64(long value) {
        int size = computeRawVarint64Size(value);
        if (size == 1) {
            buffer.writeByte((byte)value);
        } else {
            int last = size - 1;
            int i = 0;
            long nextValue = value;
            while (i < last) {
                buffer.writeByte((int)(nextValue & 0x7F) | 0x80);
                i += 1;
                nextValue >>>= 7;
            }
            buffer.writeByte((byte)nextValue);
        }
    }

    private int computeRawVarint32Size(int value) {
        if ((value & (0xffffffff << 7)) == 0) return 1;
        if ((value & (0xffffffff << 14)) == 0) return 2;
        if ((value & (0xffffffff << 21)) == 0) return 3;
        if ((value & (0xffffffff << 28)) == 0) return 4;
        return 5;
    }

    /**
     * Compute the number of bytes that would be needed to encode a varint.
     */
    private int computeRawVarint64Size(long value){
        if ((value & (0xffffffffffffffffL << 7)) == 0) return 1;
        if ((value & (0xffffffffffffffffL << 14)) == 0) return 2;
        if ((value & (0xffffffffffffffffL << 21)) == 0) return 3;
        if ((value & (0xffffffffffffffffL << 28)) == 0) return 4;
        if ((value & (0xffffffffffffffffL << 35)) == 0) return 5;
        if ((value & (0xffffffffffffffffL << 42)) == 0) return 6;
        if ((value & (0xffffffffffffffffL << 49)) == 0) return 7;
        if ((value & (0xffffffffffffffffL << 56)) == 0) return 8;
        if ((value & (0xffffffffffffffffL << 63)) == 0) return 9;
        return 10;
    }


    private void writeRawLittleEndian32(int value) {
        buffer.writeByte(value & 0xFF);
        buffer.writeByte(value >> 8 & 0xFF);
        buffer.writeByte(value >> 16 & 0xFF);
        buffer.writeByte(value >> 24 & 0xFF);
        //return LITTLE_ENDIAN_32_SIZE;
    }


    private void writeRawLittleEndian64(long value) {
        buffer.writeByte((byte)(value & 0xFF));
        buffer.writeByte((byte)(value >> 8 & 0xFF)) ;
        buffer.writeByte((byte)(value >> 16 & 0xFF));
        buffer.writeByte((byte)(value >> 24 & 0xFF));
        buffer.writeByte((byte)(value >> 32 & 0xFF));
        buffer.writeByte((byte)(value >> 40 & 0xFF));
        buffer.writeByte((byte)(value >> 48 & 0xFF));
        buffer.writeByte((byte)(value >> 56 & 0xFF));
        //return LITTLE_ENDIAN_64_SIZE;
    }

    private int encodeZigZag32(int n) {
        return (n << 1) ^ (n >> 31);
    }

    private long encodeZigZag64(long n) {
        return (n << 1) ^ (n >> 63);
    }
}

