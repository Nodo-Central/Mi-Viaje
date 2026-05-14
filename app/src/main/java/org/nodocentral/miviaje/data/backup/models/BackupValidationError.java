package org.nodocentral.miviaje.data.backup.models;

public class BackupValidationError {
    public final String fieldPath;
    public final String code;
    public final String message;

    public BackupValidationError(String fieldPath, String code, String message) {
        this.fieldPath = fieldPath;
        this.code = code;
        this.message = message;
    }
}
