package org.nodocentral.miviaje.data.nfc.desfire.payloads;

import java.nio.ByteOrder;

public class DesfireSelectApplicationPayload extends DesfireAbstractPayload {
    private final byte[] applicationId;
    public DesfireSelectApplicationPayload(int applicationId) {
        this.applicationId = intToBytes(applicationId, ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public byte[] toBytes() {
        return this.applicationId;
    }
}
