package org.nodocentral.miviaje.data.nfc.desfire.payloads;

public class DesfireReadPayload extends DesfireAbstractPayload {
    private final int fileNo;
    private final int offset;
    private final int length;

    public DesfireReadPayload(int fileNo, int offset, int length) {
        this.fileNo = fileNo;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public byte[] toBytes() {
        byte[] offsetBytes = intToBytes(offset);   // little-endian
        byte[] lengthBytes = intToBytes(length);   // little-endian

        byte[] result = new byte[1 + offsetBytes.length + lengthBytes.length];
        int idx = 0;

        result[idx++] = (byte) fileNo;
        System.arraycopy(offsetBytes, 0, result, idx, offsetBytes.length);
        idx += offsetBytes.length;
        System.arraycopy(lengthBytes, 0, result, idx, lengthBytes.length);

        return result;
    }
}
