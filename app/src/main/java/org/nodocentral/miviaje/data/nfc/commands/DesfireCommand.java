package org.nodocentral.miviaje.data.nfc.commands;

import org.nodocentral.miviaje.data.nfc.desfire.payloads.DesfirePayload;
import org.nodocentral.miviaje.data.nfc.desfire.NativeCommand;

public class DesfireCommand extends ApduCommand {
    private static final byte DESFIRE_CLA = (byte) 0x90;

    public DesfireCommand(NativeCommand ins, int p1, int p2, DesfirePayload payload) {
        super(DESFIRE_CLA, ins.getCode(), p1, p2, payload != null ? payload.toBytes() : new byte[]{});
    }

    public DesfireCommand(NativeCommand ins, DesfirePayload payload) {
        this(ins, 0x00, 0x00, payload);
    }

    public DesfireCommand(NativeCommand ins, int p1, int p2) {
        this(ins, p1, p2, null);
    }

    public DesfireCommand(NativeCommand ins) {
        this(ins, 0x00, 0x00);
    }
}
