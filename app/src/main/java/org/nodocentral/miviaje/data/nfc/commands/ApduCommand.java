package org.nodocentral.miviaje.data.nfc.commands;

import androidx.annotation.NonNull;

public class ApduCommand {
    private final byte cla;
    private final byte ins;
    private final byte p1;
    private final byte p2;
    private final byte[] data;  // may be null or empty
    private final byte le;      // may be null if absent

    public ApduCommand(int cla, int ins, int p1, int p2, byte[] data, int le) {
        this.cla = (byte) cla;
        this.ins = (byte) ins;
        this.p1 = (byte) p1;
        this.p2 = (byte) p2;
        this.data = data;
        this.le = (byte) le;
    }

    public ApduCommand(int cla, int ins, int p1, int p2, byte[] data) {
        this(cla, ins, p1, p2, data, 0x0);
    }

    public ApduCommand(int cla, int ins, int p1, int p2) {
        this(cla, ins, p1, p2, new byte[]{});
    }

    public byte getCla() {
        return cla;
    }

    public byte getIns() {
        return ins;
    }

    public byte getP1() {
        return p1;
    }

    public byte getP2() {
        return p2;
    }

    public byte[] getData() {
        return data.clone();
    }

    public Byte getLe() {
        return le;
    }

    /**
     * Build the raw APDU byte array: [CLA, INS, P1, P2, Lc, data..., Le]?
     */
    public byte[] toByteArray() {
        int dataLen = data.length;
        // total = header(4) + Lc(1) + dataLen + Le(1)
        byte[] apdu = new byte[4 + (dataLen > 0 ? 1 : 0) + dataLen + 1];
        int idx = 0;
        apdu[idx++] = cla;
        apdu[idx++] = ins;
        apdu[idx++] = p1;
        apdu[idx++] = p2;
        if (dataLen > 0) {
            apdu[idx++] = (byte) dataLen;
            System.arraycopy(data, 0, apdu, idx, dataLen);
            idx += dataLen;
        }
        apdu[idx] = le;
        return apdu;
    }

    private String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return "[" + sb.toString().trim() + "]";
    }

    @NonNull
    @Override
    public String toString() {
        return "ApduCommand[" +
                "cla=" + String.format("%02X", cla) +
                ", ins=" + String.format("%02X", ins) +
                ", p1=" + String.format("%02X", p1) +
                ", p2=" + String.format("%02X", p2) +
                ", data=" + bytesToString(data) +
                ", le=" + String.format("%02X", le) +
                ", command=" + bytesToString(toByteArray())+
                "]";
    }
}
