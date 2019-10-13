package io.protostuff;

import io.netty.buffer.ByteBuf;
import io.protostuff.StringSerializer.STRING;

import java.io.IOException;
import java.nio.ByteBuffer;

import static io.protostuff.WireFormat.*;

public class ByteBufProtobufInput implements Input {
    private final ByteBuf buffer;
    private boolean decodeNestedMessageAsGroup;
    private int lastTag = 0;
    private int packedLimit = 0;

    public ByteBufProtobufInput(ByteBuf byteBuf) {
        this.buffer = byteBuf.slice();
    }

    public ByteBufProtobufInput(ByteBuf byteBuf, boolean decodeNestedMessageAsGroup) {
        this.buffer = byteBuf.slice();
        this.decodeNestedMessageAsGroup = decodeNestedMessageAsGroup;
    }

    /**
     * Resets the offset and the limit of the internal buffer.
     */
    public ByteBufProtobufInput reset(int offset, int len) {
        buffer.readerIndex(0);
        return this;
    }

    /**
     * Return true if currently reading packed field
     */
    public boolean isCurrentFieldPacked() {
        return packedLimit != 0 && packedLimit != buffer.readerIndex();
    }

    /**
     * Returns the last tag.
     */
    public int getLastTag() {
        return lastTag;
    }

    /**
     * Attempt to read a field tag, returning zero if we have reached EOF. Protocol message parsers use this to read
     * tags, since a protocol message may legally end wherever a tag occurs, and zero is not a valid tag number.
     */
    public int readTag() throws IOException {
        if (!buffer.isReadable()) {
            lastTag = 0;
            return 0;
        }
        int tag = readRawVarint32();
        if ((tag >>> TAG_TYPE_BITS) == 0) { // If we actually read zero, that's not a valid tag.
            throw ProtobufException.invalidTag();
        }
        lastTag = tag;
        return tag;
    }

    /**
     * Verifies that the last call to readTag() returned the given tag value. This is used to verify that a nested group
     * ended with the correct end tag.
     *
     * @throws ProtobufException
     * { @code value} does not match the last tag.
     */
    public void checkLastTagWas(int value) throws IOException {
        if (lastTag != value) throw ProtobufException.invalidEndTag();
    }

    /**
     * Reads and discards a single field, given its tag value.
     *
     * @return { @code false} if the tag is an endgroup tag, in which case nothing is skipped. Otherwise, returns
     *                 { @code true}.
     */
    public boolean skipField(int tag) throws IOException {
        switch (getTagWireType(tag)) {
            case WIRETYPE_VARINT:
                readInt32();
                return true;
            case WIRETYPE_FIXED64:
                readRawLittleEndian64();
                return true;
            case WIRETYPE_LENGTH_DELIMITED:
                int size = readRawVarint32();
                if (size < 0) throw ProtobufException.negativeSize();
                buffer.readerIndex(buffer.readerIndex() + size);
                return true;
            case WIRETYPE_START_GROUP:
                skipMessage();
                checkLastTagWas(makeTag(getTagFieldNumber(tag), WIRETYPE_END_GROUP));
                return true;
            case WIRETYPE_END_GROUP:
                return false;
            case WIRETYPE_FIXED32:
                readRawLittleEndian32();
                return true;
            default:
                throw ProtobufException.invalidWireType();
        }
    }

    /**
     * Reads and discards an entire message. This will read either until EOF or until an endgroup tag, whichever comes
     * first.
     */
    public void skipMessage() throws IOException {
        while (true) {
            int tag = readTag();
            if (tag == 0 || !skipField(tag)) return;
        }
    }

    public <T> void handleUnknownField(int fieldNumber, Schema<T> schema) throws IOException {
        skipField(lastTag);
    }

    public <T> int readFieldNumber(Schema<T> schema) throws IOException {
        if (!buffer.isReadable()) {
            lastTag = 0;
            return 0;
        }
        // are we reading packed field?
        if (isCurrentFieldPacked()) {
            if (packedLimit < buffer.readerIndex()) throw ProtobufException.misreportedSize();
            // Return field number while reading packed field
            return lastTag >>> TAG_TYPE_BITS;
        }
        packedLimit = 0;
        int tag = readRawVarint32();
        int fieldNumber = tag >>> TAG_TYPE_BITS;
        if (fieldNumber == 0) {
            if (decodeNestedMessageAsGroup && WIRETYPE_TAIL_DELIMITER == (tag & TAG_TYPE_MASK)) { // protostuff's tail delimiter for streaming
                // 2 options: length-delimited or tail-delimited.
                lastTag = 0;
                return 0;
            }
            throw ProtobufException.invalidTag();
        }
        if (decodeNestedMessageAsGroup && WIRETYPE_END_GROUP == (tag & TAG_TYPE_MASK)) {
            lastTag = 0;
            return 0;
        }
        lastTag = tag;
        return fieldNumber;
    }

    /**
     * Check if this field have been packed into a length-delimited field. If so, update internal state to reflect that
     * packed fields are being read.
     *
     */

    private void checkIfPackedField() throws IOException { // Do we have the start of a packed field?
        if (packedLimit == 0 && getTagWireType(lastTag) == WIRETYPE_LENGTH_DELIMITED) {
            int length = readRawVarint32();
            if (length < 0) throw ProtobufException.negativeSize();
            if (buffer.readerIndex() + length > buffer.capacity()) throw ProtobufException.misreportedSize();
            this.packedLimit = buffer.readerIndex() + length;
        }
    }

    public double readDouble() throws IOException {
        checkIfPackedField();
        return Double.longBitsToDouble(readRawLittleEndian64());
    }

    public float readFloat() throws IOException {
        checkIfPackedField();
        return Float.intBitsToFloat(readRawLittleEndian32());
    }

    public long readUInt64() throws IOException {
        checkIfPackedField();
        return readRawVarint64();
    }

    public long readInt64() throws IOException {
        checkIfPackedField();
        return readRawVarint64();
    }

    public int readInt32() throws IOException {
        checkIfPackedField();
        return readRawVarint32();
    }

    public long readFixed64() throws IOException {
        checkIfPackedField();
        return readRawLittleEndian64();
    }

    public int readFixed32() throws IOException {
        checkIfPackedField();
        return readRawLittleEndian32();
    }


    public boolean readBool() throws IOException {
        checkIfPackedField();
        return buffer.readByte() != 0;
    }


    public int readUInt32() throws IOException {
        checkIfPackedField();
        return  readRawVarint32();
    }

    /**
     * Read an enum field value from the internal buffer. Caller is responsible for converting the numeric value to an
     * actual enum.
     */
    public int readEnum() throws IOException  {
        checkIfPackedField();
        return readRawVarint32();
    }

    public int readSFixed32() throws IOException {
        checkIfPackedField();
        return readRawLittleEndian32();
    }


    public long readSFixed64() throws IOException {
        checkIfPackedField();
        return readRawLittleEndian64();
    }

    public int readSInt32() throws IOException {
        checkIfPackedField();
        int n = readRawVarint32();
        return (n >>> 1) ^ -(n & 1);
    }

    public long readSInt64() throws IOException {
        checkIfPackedField();
        long n = readRawVarint64();
        return (n >>> 1) ^ -(n & 1);
    }

    public String readString() throws IOException {
        int length = readRawVarint32();
        if (length < 0) throw ProtobufException.negativeSize();
        if (buffer.readableBytes() < length) throw ProtobufException.misreportedSize();

        if (buffer.hasArray()) {
            int currPosition = buffer.readerIndex();
            buffer.readerIndex(buffer.readerIndex() + length);
            return STRING.deser(buffer.array(), buffer.arrayOffset() + currPosition, length);
        } else {
            byte[] tmp = new byte[length];
            buffer.readBytes(tmp);
            return STRING.deser(tmp);
        }
    }


    public ByteString readBytes() throws IOException {
        return ByteString.wrap(readByteArray());
    }

    public void readBytes(ByteBuffer bb) throws IOException {
        int length = readRawVarint32();
        if (length < 0) throw ProtobufException.negativeSize();
        if (buffer.readableBytes() < length) throw ProtobufException.misreportedSize();
        buffer.readBytes(bb);
    }


    public byte[] readByteArray() throws IOException {
        int length = readRawVarint32();
        if (length < 0) throw ProtobufException.negativeSize();
        if (buffer.readableBytes() < length) throw ProtobufException.misreportedSize();
        byte[] copy = new byte[length];
        buffer.readBytes(copy);
        return copy;
    }


    public <T> T mergeObject(T objValue, Schema<T> schema) throws IOException {
        if (decodeNestedMessageAsGroup) return mergeObjectEncodedAsGroup(objValue, schema);
        int length = readRawVarint32();
        if (length < 0) throw ProtobufException.negativeSize();
        if (buffer.readableBytes() < length) throw ProtobufException.misreportedSize();
        T value = objValue;
        if (value == null) value = schema.newMessage();
        int readerIndex = buffer.readerIndex();
        ByteBuf dup = buffer.slice(readerIndex, length);
        ByteBufProtobufInput nestedInput = new ByteBufProtobufInput(dup, decodeNestedMessageAsGroup);
        schema.mergeFrom(nestedInput, value);
        if (!schema.isInitialized(value)) throw new UninitializedMessageException(value, schema);
        nestedInput.checkLastTagWas(0);

        buffer.readerIndex(readerIndex + length);
        return value;
    }


    private <T> T mergeObjectEncodedAsGroup(T objValue, Schema<T> schema) throws IOException {
        T value = objValue;
        if (value == null) value = schema.newMessage();
        schema.mergeFrom(this, value);
        if (!schema.isInitialized(value)) throw new UninitializedMessageException(value, schema);
        checkLastTagWas(0);
        return value;
    }

    /**
     * Reads a var int 32 from the internal byte buffer.
     */
    private int readRawVarint32() throws IOException {
        int tmp = buffer.readByte();
        if (tmp >= 0) return tmp;
        int result = tmp & 0x7f;
        if ((tmp = buffer.readByte()) >= 0) result |= tmp << 7;
        else {
            result |= (tmp & 0x7f) << 7;
            if ((tmp = buffer.readByte()) >= 0) result |= tmp << 14;
            else {
                result |= (tmp & 0x7f) << 14;
                if ((tmp = buffer.readByte()) >= 0) result |= tmp << 21;
                else {
                    result |= (tmp & 0x7f) << 21;
                    result |= (tmp = buffer.readByte()) << 28;
                    if (tmp < 0) { // Discard upper 32 bits.
                        int i = 0;
                        while (i < 5) {
                            if (buffer.readByte() >= 0) return result;
                            i += 1;
                        }
                        throw ProtobufException.malformedVarint();
                    }
                }
            }
        }
        return result;
    }

    /**
     * Reads a var int 64 from the internal byte buffer.
     */
    public long readRawVarint64() throws IOException {
        int shift = 0;
        long result = 0L;
        while (shift < 64) {
            byte b = buffer.readByte();
            result |= ((long)(b & 0x7F)) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
            shift += 7;
        }
        throw ProtobufException.malformedVarint();
    }

    /**
     * Read a 32-bit little-endian integer from the internal buffer.
     */
    public int readRawLittleEndian32() throws IOException {
        return buffer.readByte() & 0xff | ((buffer.readByte() & 0xff) << 8) | ((buffer.readByte() & 0xff) << 16) | ((buffer.readByte() & 0xff) << 24);
    }

    /**
     * Read a 64-bit little-endian integer from the internal byte buffer.
     */
    public long readRawLittleEndian64() throws IOException {
        long result = ((long)buffer.readByte()) & 0xff;
        int shift = 8;
        while (shift < 64) {
            result |= ((((long)buffer.readByte()) & 0xff) << shift);
            shift += 8;
        }
        return result;
    }

    public void transferByteRangeTo(Output output, boolean utf8String, int fieldNumber, boolean repeated) throws IOException {
        int length = readRawVarint32();
        if (length < 0) throw ProtobufException.negativeSize();
        if (buffer.readableBytes() < length) throw ProtobufException.misreportedSize();
        if (buffer.hasArray()) {
            output.writeByteRange(utf8String, fieldNumber, buffer.array(), buffer.arrayOffset() + buffer.readerIndex(), length, repeated);
            buffer.readerIndex(buffer.readerIndex() + length);
        } else {
            byte[] bytes = new byte[length];
            buffer.readBytes(bytes);
            output.writeByteRange(utf8String, fieldNumber, bytes, 0, bytes.length, repeated);
        }
    }

    /**
     * Reads a byte array/ByteBuffer value.
     */
    public ByteBuffer readByteBuffer() throws IOException {
        return ByteBuffer.wrap(readByteArray());
    }
}
