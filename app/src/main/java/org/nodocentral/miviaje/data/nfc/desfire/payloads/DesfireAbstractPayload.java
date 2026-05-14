package org.nodocentral.miviaje.data.nfc.desfire.payloads;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public abstract class DesfireAbstractPayload implements DesfirePayload {
    private static final int OFFSET = 0;
    private static final int LENGTH = 3;
    static byte[] intToBytes(int value, ByteOrder endianness) {
        ByteBuffer bb = ByteBuffer.allocate(4).order(endianness);
        bb.putInt(value);
        return Arrays.copyOfRange(bb.array(), OFFSET, LENGTH);
    }

    static byte[] intToBytes(int value) {
        ByteBuffer bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(value);
        return Arrays.copyOfRange(bb.array(), OFFSET + 1, LENGTH + 1);
    }
}
