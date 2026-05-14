package org.nodocentral.miviaje.data.backup.importing;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.nodocentral.miviaje.data.backup.models.BackupImportResult;
import org.nodocentral.miviaje.data.backup.models.BackupValidationError;
import org.nodocentral.miviaje.data.backup.schema.BackupSchemaMapper;
import org.nodocentral.miviaje.data.backup.schema.BackupSchemaV2Mapper;
import org.nodocentral.miviaje.data.backup.schema.BackupSchemaV3Mapper;

import java.util.ArrayList;
import java.util.List;

public class BackupImportService {
    private final BackupImporter backupImporter;

    public BackupImportService(BackupImporter backupImporter) {
        this.backupImporter = backupImporter;
    }

    public BackupImportResult importData(String json) {
        JsonObject root;
        try {
            root = JsonParser.parseString(json).getAsJsonObject();
        } catch (JsonParseException | IllegalStateException exception) {
            return BackupImportResult.failure(List.of(new BackupValidationError("$", "INVALID_JSON", "Backup is not a valid JSON object")));
        }

        List<BackupValidationError> errors = new ArrayList<>();
        Integer schemaVersion = validateRootAndSchema(root, errors);
        if (!errors.isEmpty()) {
            return BackupImportResult.failure(errors);
        }

        BackupSchemaMapper mapper = schemaVersion == 2
                ? new BackupSchemaV2Mapper()
                : new BackupSchemaV3Mapper();

        return backupImporter.importSnapshot(root, mapper);
    }

    private Integer validateRootAndSchema(JsonObject root, List<BackupValidationError> errors) {
        if (!root.has("schemaVersion") || root.get("schemaVersion").isJsonNull()) {
            errors.add(new BackupValidationError("$.schemaVersion", "REQUIRED", "schemaVersion is required"));
            return null;
        }

        int schemaVersion;
        try {
            schemaVersion = root.get("schemaVersion").getAsInt();
        } catch (RuntimeException exception) {
            errors.add(new BackupValidationError("$.schemaVersion", "INVALID_TYPE", "schemaVersion must be an integer"));
            return null;
        }

        if (schemaVersion != 2 && schemaVersion != 3) {
            errors.add(new BackupValidationError("$.schemaVersion", "UNSUPPORTED_VERSION", "Only schema versions 2 and 3 are supported"));
            return null;
        }

        validateArray(root, "cards", errors);
        validateArray(root, "products", errors);
        validateArray(root, "events", errors);
        if (schemaVersion >= 3 && root.has("artworks") && !root.get("artworks").isJsonArray()) {
            errors.add(new BackupValidationError("$.artworks", "INVALID_TYPE", "artworks must be an array when provided"));
        }

        return schemaVersion;
    }

    private void validateArray(JsonObject root, String key, List<BackupValidationError> errors) {
        if (!root.has(key) || root.get(key).isJsonNull()) {
            errors.add(new BackupValidationError("$." + key, "REQUIRED", key + " array is required"));
            return;
        }
        if (!root.get(key).isJsonArray()) {
            errors.add(new BackupValidationError("$." + key, "INVALID_TYPE", key + " must be an array"));
        }
    }
}
