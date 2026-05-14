package org.nodocentral.miviaje.data.nfc.desfire;

import android.nfc.tech.IsoDep;

import org.nodocentral.miviaje.data.nfc.commands.DesfireCommand;
import org.nodocentral.miviaje.data.nfc.desfire.payloads.DesfireGetValuePayload;
import org.nodocentral.miviaje.data.nfc.desfire.payloads.DesfireReadPayload;
import org.nodocentral.miviaje.data.nfc.desfire.payloads.DesfireSelectApplicationPayload;
import org.nodocentral.miviaje.data.nfc.responses.DesfireResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DesfireManager {
    private final IsoDep isoDep;

    public DesfireManager(IsoDep isoDep) {
        this.isoDep = isoDep;
    }

    private DesfireResponse transceive(DesfireCommand command) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DesfireCommand readMoreFramesCommand = new DesfireCommand(NativeCommand.ADDITIONAL_FRAME);
        DesfireResponse response;

        response = new DesfireResponse(isoDep.transceive(command.toByteArray()));
        while (response.hasMoreFrames()) {
            byteStream.write(response.getData(), 0, response.getLength());
            response = transceive(readMoreFramesCommand);
        }
        byteStream.write(response.getData(), 0, response.getLength());
        response = new DesfireResponse(byteStream.toByteArray(), response.getSw1(), response.getSw2());

        if (!response.success())
            throw new IOException("Status: " + response.getStatus().getDescription());

        return response;
    }

    public DesfireResponse selectApplication(int applicationId) throws IOException {
        DesfireCommand command = new DesfireCommand(
                NativeCommand.SELECT_APPLICATION,
                new DesfireSelectApplicationPayload(applicationId)
        );
        return transceive(command);
    }

    public DesfireResponse getCardUid() throws IOException {
        DesfireCommand command = new DesfireCommand(
                NativeCommand.GET_CARD_UID
        );
        return transceive(command);
    }

    public DesfireResponse getVersion() throws IOException {
        DesfireCommand command = new DesfireCommand(
                NativeCommand.GET_VERSION
        );
        return transceive(command);
    }

    public DesfireResponse readData(int fileNo, int offset, int length) throws IOException {
        DesfireCommand command = new DesfireCommand(
                NativeCommand.READ_DATA,
                new DesfireReadPayload(fileNo, offset, length)
        );
        return transceive(command);
    }

    public DesfireResponse getValue(int fileNo) throws IOException {
        DesfireCommand command = new DesfireCommand(
                NativeCommand.GET_VALUE,
                new DesfireGetValuePayload(fileNo)
        );
        return transceive(command);
    }

    public DesfireResponse readRecords(int fileNo, int firstRecord, int numRecords) throws IOException {
        DesfireCommand command = new DesfireCommand(
                NativeCommand.READ_RECORDS,
                new DesfireReadPayload(fileNo, firstRecord, numRecords)
        );
        return transceive(command);
    }
}
