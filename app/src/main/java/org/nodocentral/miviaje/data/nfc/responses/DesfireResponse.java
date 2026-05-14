package org.nodocentral.miviaje.data.nfc.responses;

import androidx.annotation.NonNull;

import org.nodocentral.miviaje.data.nfc.desfire.StatusCode;

import java.util.Arrays;

public class DesfireResponse extends ApduResponse {
    private StatusCode status;

    public DesfireResponse(byte[] response, byte sw1, byte sw2) {
        super(response, sw1, sw2);
        status = StatusCode.fromCode(sw2);
    }

    public DesfireResponse(byte[] fullResponse) {
        this(Arrays.copyOf(fullResponse, fullResponse.length - 2),
                fullResponse[fullResponse.length - 2],
                fullResponse[fullResponse.length - 1]);
    }

    public StatusCode getStatus() {
        return this.status;
    }

    public boolean success() {
        return status == StatusCode.SUCCESS;
    }

    public boolean hasMoreFrames() {
        return status == StatusCode.ADDITIONAL_FRAMES;
    }

    @NonNull
    @Override
    public String toString() {
        return "DesfireResponse[data=" + bytesToString(data) +
                ", SW1=0x" + String.format("%02X", sw1) +
                ", SW2=0x" + String.format("%02X", sw2) +
                ", status=" + status.name() +
                "]";
    }
}
