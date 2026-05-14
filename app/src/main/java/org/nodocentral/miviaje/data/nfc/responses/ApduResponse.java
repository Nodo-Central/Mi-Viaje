package org.nodocentral.miviaje.data.nfc.responses;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ApduResponse {
    protected final byte[] data;
    protected final byte sw1;
    protected final byte sw2;

    public ApduResponse(byte[] data, byte sw1, byte sw2) {
        this.data = (data != null) ? data.clone() : new byte[0];
        this.sw1  = sw1;
        this.sw2  = sw2;
    }

    /**
     * Parses a raw APDU response byte‐array of form [data..., SW1, SW2].
     * @param fullResponse the raw response bytes
     * @throws IllegalArgumentException if length &lt; 2
     */
    public ApduResponse(byte[] fullResponse) {
        this(Arrays.copyOf(fullResponse, fullResponse.length - 2),
                fullResponse[fullResponse.length - 2],
                fullResponse[fullResponse.length - 1]);
    }

    public byte[] getData() {
        return data.clone();
    }

    public int getLength() { return data.length; }

    public byte getSw1() {
        return sw1;
    }

    public byte getSw2() {
        return sw2;
    }

    public ByteBuffer getBuffer() {
        return ByteBuffer.wrap(data.clone());
    }

    public int getStatusWord() {
        return ((sw1 & 0xFF) << 8) | (sw2 & 0xFF);
    }

    public byte[] toByteArray() {
        byte[] resp = new byte[data.length + 2];
        System.arraycopy(data, 0, resp, 0, data.length);
        resp[data.length]     = sw1;
        resp[data.length + 1] = sw2;
        return resp;
    }

    protected String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return "[" + sb.toString().trim() + "]";
    }

    @NonNull
    @Override
    public String toString() {
        return "ApduResponse[data=" + bytesToString(data) +
                ", SW1=0x" +
                String.format("%02X", sw1) +
                ", SW2=0x" +
                String.format("%02X", sw2) +
                "]";
    }
}
