package org.nodocentral.miviaje.data.backup.schema;

import com.google.gson.JsonObject;

import org.nodocentral.miviaje.data.backup.models.BackupSnapshot;

public interface BackupSchemaMapper {
    BackupSnapshot map(JsonObject root);
}
