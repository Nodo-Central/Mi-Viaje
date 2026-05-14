package org.nodocentral.miviaje.data.backup.importing;

import com.google.gson.JsonObject;

import org.nodocentral.miviaje.data.backup.models.BackupImportResult;
import org.nodocentral.miviaje.data.backup.models.BackupValidationError;
import org.nodocentral.miviaje.data.backup.schema.BackupMapperSupport;
import org.nodocentral.miviaje.data.backup.schema.BackupSchemaMapper;

import java.util.List;

public class BackupImporter {
    public BackupImportResult importSnapshot(JsonObject root, BackupSchemaMapper mapper) {
        try {
            return BackupImportResult.success(mapper.map(root));
        } catch (BackupMapperSupport.BackupFieldException fieldException) {
            return BackupImportResult.failure(List.of(fieldException.asValidationError()));
        } catch (RuntimeException runtimeException) {
            return BackupImportResult.failure(List.of(new BackupValidationError(
                    "$.payload",
                    "INVALID_PAYLOAD",
                    runtimeException.getMessage() == null ? "Unable to map backup payload" : runtimeException.getMessage()
            )));
        }
    }
}
