package org.nodocentral.miviaje.data.nfc.desfire;

import androidx.annotation.NonNull;

public enum StatusCode {
    SUCCESS              (0x00, "Success"),
    NO_CHANGE            (0x0C, "No change"),
    OUT_OF_EEPROM        (0x0E, "Out of EEPROM"),
    ILLEGAL_COMMAND      (0x1C, "Illegal command"),
    INTEGRITY_ERROR      (0x1E, "Integrity error"),
    NO_SUCH_KEY          (0x40, "No such key"),
    ISO_ERROR            (0x6E, "ISO Error"),
    LENGTH_ERROR         (0x7E, "Length error"),
    CRYPTO_ERROR         (0x97, "Crypto error"),
    PERMISSION_DENIED    (0x9D, "Permission denied"),
    PARAMETER_ERROR      (0x9E, "Parameter error"),
    APPLICATION_NOT_FOUND(0xA0, "Application not found"),
    AUTHENTICATION_ERROR (0xAE, "Authentication error"),
    ADDITIONAL_FRAMES(0xAF, "Additional frame"),
    BOUNDARY_ERROR       (0xBE, "Boundary error"),
    CARD_INTEGRITY_ERROR (0xC1, "MiViaje.Card integrity error"),
    COMMAND_ABORTED      (0xCA, "Command aborted"),
    CARD_DISABLED        (0xCD, "MiViaje.Card disabled"),
    COUNT_ERROR          (0xCE, "Count error"),
    DUPLICATE_ERROR      (0xDE, "Duplicate error"),
    EEPROM_ERROR         (0xEE, "EEPROM error"),
    FILE_NOT_FOUND       (0xF0, "File not found"),
    FILE_INTEGRITY_ERROR (0xF1, "File integrity error");

    private final int code;
    private final String description;

    StatusCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static StatusCode fromCode(int code) {
        for (StatusCode s : values()) {
            if (s.code == (code & 0xFF)) {
                return s;
            }
        }
        throw new IllegalArgumentException(
                String.format("Unknown DESFire status code: 0x%02X", code)
        );
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%s (0x%02X): %s", name(), code, description);
    }
}
