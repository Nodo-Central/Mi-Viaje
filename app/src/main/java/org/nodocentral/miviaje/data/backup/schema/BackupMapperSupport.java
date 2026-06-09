package org.nodocentral.miviaje.data.backup.schema;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.nodocentral.miviaje.data.backup.models.BackupValidationError;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

public abstract class BackupMapperSupport {
    protected static long hexToLong(String hex) {
        return Long.parseLong(hex, 16);
    }

    protected static long hexToUnsignedLong(String hex) {
        return Long.parseUnsignedLong(hex, 16);
    }

    protected static int hexToInt(String hex) {
        return Integer.parseInt(hex, 16);
    }

    protected static short hexToShort(String hex) {
        return Short.parseShort(hex, 16);
    }

    protected static LocalDate intToDate(Integer value) {
        return value == null ? null : LocalDate.ofEpochDay(value);
    }

    protected static LocalDateTime intToDateTime(Integer value) {
        return value == null ? null : LocalDateTime.ofEpochSecond(value, 0, ZoneOffset.ofHours(-6));
    }

    protected static LocalTime intToTime(Integer value) {
        return value == null ? null : LocalTime.ofSecondOfDay(value);
    }

    protected static String getRequiredString(JsonObject object, String key, String path) {
        JsonElement value = object.get(key);
        if (value == null || value.isJsonNull()) {
            throw new BackupFieldException(path + "." + key, "missing required string");
        }
        return value.getAsString();
    }

    protected static int getRequiredInt(JsonObject object, String key, String path) {
        JsonElement value = object.get(key);
        if (value == null || value.isJsonNull()) {
            throw new BackupFieldException(path + "." + key, "missing required int");
        }
        return value.getAsInt();
    }

    protected static Integer getNullableInt(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || value.isJsonNull()) {
            return null;
        }
        return value.getAsInt();
    }

    protected static String getNullableString(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || value.isJsonNull()) {
            return null;
        }
        return value.getAsString();
    }

    public static class BackupFieldException extends RuntimeException {
        private final String fieldPath;

        public BackupFieldException(String fieldPath, String message) {
            super(message);
            this.fieldPath = fieldPath;
        }

        public BackupValidationError asValidationError() {
            return new BackupValidationError(fieldPath, "INVALID_FIELD", getMessage());
        }
    }
}
