package org.nodocentral.miviaje.data.nfc.desfire;

import androidx.annotation.NonNull;

public enum NativeCommand {
    // Security related commands
    AUTHENTICATE_AES          (0xAA, "Authenticate (AES)"),
    AUTHENTICATE_ISO          (0x1A, "Authenticate (ISO)"),
    AUTHENTICATE_LEGACY       (0x0A, "Authenticate (Legacy)"),
    CHANGE_KEY_SETTINGS       (0x54, "Change KeySettings"),
    SET_CONFIGURATION         (0x5C, "Set Configuration"),
    CHANGE_KEY                (0xC4, "Change Key"),
    GET_KEY_VERSION           (0x64, "Get Key Version"),

    // MiViaje.Card level commands
    CREATE_APPLICATION        (0xCA, "Create Application"),
    DELETE_APPLICATION        (0xDA, "Delete Application"),
    GET_APPLICATION_IDS       (0x6A, "Get Applications IDs"),
    FREE_MEMORY               (0x6E, "Free Memory"),
    GET_DF_NAMES              (0x6D, "Get DFNames"),
    GET_KEY_SETTINGS          (0x45, "Get KeySettings"),
    SELECT_APPLICATION        (0x5A, "Select Application"),
    FORMAT_PICC               (0xFC, "FormatPICC"),
    GET_VERSION               (0x60, "Get Version"),
    GET_CARD_UID              (0x51, "GetCardUID"),

    // Application level commands
    GET_FILE_IDS              (0x6F, "Get FileIDs"),
    GET_FILE_IDS_ISO          (0x61, "Get FileIDs (ISO)"),
    GET_FILE_SETTINGS         (0xF5, "Get FileSettings"),
    CHANGE_FILE_SETTINGS      (0x5F, "Change FileSettings"),
    CREATE_STD_DATA_FILE      (0xCD, "Create StdDataFile"),
    CREATE_BACKUP_DATA_FILE   (0xCB, "Create BackupDataFile"),
    CREATE_VALUE_FILE         (0xCC, "Create ValueFile"),
    CREATE_LINEAR_RECORD_FILE (0xC1, "Create LinearRecordFile"),
    CREATE_CYCLIC_RECORD_FILE (0xC0, "Create CyclicRecordFile"),
    DELETE_FILE               (0xDF, "DeleteFile"),

    // Data manipulation commands
    READ_DATA                 (0xBD, "Read Data"),
    WRITE_DATA                (0x3D, "Write Data"),
    ADDITIONAL_FRAME          (0xAF, "Additional Frame"),
    GET_VALUE                 (0x6C, "Get Value"),
    CREDIT                    (0x0C, "Credit"),
    DEBIT                     (0xDC, "Debit"),
    LIMITED_CREDIT            (0x1C, "Limited Credit"),
    WRITE_RECORD              (0x3B, "Write Record"),
    READ_RECORDS              (0xBB, "Read Records"),
    CLEAR_RECORD_FILE         (0xEB, "Clear RecordFile"),
    COMMIT_TRANSACTION        (0xC7, "Commit Transaction"),
    ABORT_TRANSACTION         (0xA7, "Abort Transaction");

    private final int code;
    private final String description;

    NativeCommand(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static NativeCommand fromCode(int code) {
        for (NativeCommand cmd : values()) {
            if (cmd.code == code) {
                return cmd;
            }
        }
        throw new IllegalArgumentException(
                String.format("Unknown DESFire command code: 0x%02X", code & 0xFF)
        );
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%s (0x%02X): %s", name(), code & 0xFF, description);
    }
}
