package org.nodocentral.miviaje.data.nfc.desfire.payloads;

public class DesfireGetValuePayload implements DesfirePayload {
    public final byte fileNo;
    public DesfireGetValuePayload(int fileNo) {
        this.fileNo = (byte) fileNo;
    }
    @Override
    public byte[] toBytes() {
        return new byte[]{fileNo};
    }
}
