package org.nodocentral.miviaje.data.backup.models;

import java.util.Collections;
import java.util.List;

public class BackupImportResult {
    private final BackupSnapshot snapshot;
    private final List<BackupValidationError> errors;

    private BackupImportResult(BackupSnapshot snapshot, List<BackupValidationError> errors) {
        this.snapshot = snapshot;
        this.errors = errors;
    }

    public static BackupImportResult success(BackupSnapshot snapshot) {
        return new BackupImportResult(snapshot, Collections.emptyList());
    }

    public static BackupImportResult failure(List<BackupValidationError> errors) {
        return new BackupImportResult(null, errors);
    }

    public boolean isSuccess() {
        return snapshot != null;
    }

    public BackupSnapshot getSnapshot() {
        return snapshot;
    }

    public List<BackupValidationError> getErrors() {
        return errors;
    }
}
