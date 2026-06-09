package org.nodocentral.miviaje;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.nodocentral.miviaje.data.parsers.MiMovilidadParser;

import java.nio.ByteBuffer;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

public class Helpers {
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    public static int unsign(byte n) {
        return Byte.toUnsignedInt(n);
    }

    public static short unsignShort(byte n) {
        return (short) Byte.toUnsignedInt(n);
    }

    public static long unsignLong(byte n) {
        return Byte.toUnsignedLong(n);
    }

    public static int unsign(short n) {
        return Short.toUnsignedInt(n);
    }

    public static long unsignLong(short n) {
        return Short.toUnsignedLong(n);
    }

    public static long unsign(int n) {
        return Integer.toUnsignedLong(n);
    }

    public static long unsignLong(int n) {
        return Integer.toUnsignedLong(n);
    }

    public static String intToBinaryString(int i) {
        return intToBinaryString(i, 1);
    }

    public static String intToBinaryString(int i, int chars) {
        return String.format("%" + chars + "s", Integer.toBinaryString(i))
                .replace(" ", "0");
    }

    public static String longToHex(long i) {
        return longToHex(i, 14);
    }

    public static String longToHex(long i, int chars) {
        return String.format("%0" + chars + "X", i);
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return null;
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            out[i * 2]     = HEX[v >>> 4];
            out[i * 2 + 1] = HEX[v & 0x0F];
        }
        return new String(out);
    }

    public static byte[] hexToBytes(String hex) {
        if (hex == null) return null;
        hex = hex.trim();
        if (hex.isEmpty()) return new byte[0];

        // permitir 0x al inicio
        if (hex.startsWith("0x") || hex.startsWith("0X")) hex = hex.substring(2);

        if ((hex.length() & 1) != 0) {
            throw new IllegalArgumentException("Hex length must be even");
        }

        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(hex.charAt(i), 16);
            int lo = Character.digit(hex.charAt(i + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalArgumentException("Invalid hex chars at " + i);
            }
            out[i / 2] = (byte) ((hi << 4) | lo);
        }
        return out;
    }

    public static JsonArray optArray(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return (el != null && el.isJsonArray()) ? el.getAsJsonArray() : new JsonArray();
    }

    public static String optString(JsonObject obj, String key, String def) {
        JsonElement el = obj.get(key);
        return (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isString())
                ? el.getAsString()
                : def;
    }

    public static int optInt(JsonObject obj, String key, int def) {
        JsonElement el = obj.get(key);
        try {
            return (el != null && el.isJsonPrimitive()) ? el.getAsInt() : def;
        } catch (Exception ignored) {
            return def;
        }
    }

    public static Integer optIntObj(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        try {
            return (el != null && el.isJsonPrimitive()) ? el.getAsInt() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Long optLongObj(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        try {
            return (el != null && el.isJsonPrimitive()) ? el.getAsLong() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Parsea strings tipo "[1, 2, -3]" a byte[].
     * Si viene null o formato raro, regresa null.
     */
    public static byte[] optByteArrayFromArraysToString(JsonObject obj, String key) {
        String s = optString(obj, key, null);
        if (s == null) return null;

        s = s.trim();
        if (s.isEmpty()) return null;

        // Espera formato "[...]" como Arrays.toString
        if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length() - 1).trim();
            if (s.isEmpty()) return new byte[0];

            String[] parts = s.split(",");
            byte[] out = new byte[parts.length];
            for (int i = 0; i < parts.length; i++) {
                try {
                    out[i] = (byte) Integer.parseInt(parts[i].trim());
                } catch (Exception ex) {
                    return null; // o decide ignorar ese byte
                }
            }
            return out;
        }

        return null;
    }

    public static LocalDate parseDateCompact(int timestamp) {
        int year = (timestamp >> 9) + MiMovilidadParser.EPOCH_DATE_TIME_COMPACT.getYear();
        int month = (timestamp >> 5 & 0xF);
        int day = (timestamp & 0x1F);

        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            Log.e("PARSE_DATE_COMPACT", String.format("Error parsing date (%s): %s", to32(timestamp), e));
            return null;
        }
    }

    public static LocalTime parseTimeCompact(int timestamp) {
        /* ---- TimeCompact ---- */
        int hour = (timestamp >> 11);
        int minute = (timestamp >> 5 & 0x3F);
        int second = (timestamp & 0x1F) * 2;
        // F.25  F.5    F.25
        // HHHHH MMMMMM SSSSS

        try {
            return LocalTime.of(hour, minute, second);
        } catch (DateTimeException e) {
            Log.e("PARSE_TIME_COMPACT", String.format("Error parsing time (%s): %s", to32(timestamp), e));
            return null;
        }
    }

    /**
     * Decodes a 32-bit DateTimeCompact value into date and time components.
     *
     * @param timestamp compact timestamp with year, month, day, hour, minute, and second fields.
     * @return a LocalDateTime corresponding to the decoded timestamp.
     */
    public static LocalDateTime parseDateTimeCompact(int timestamp) {
        return parseDateTimeCompact(timestamp, true);
    }

    /**
     * Decodes a 32-bit DateTimeCompact value into date and time components.
     *
     * @param timestamp compact timestamp with year, month, day, hour, minute, and second fields.
     * @param silent whether to log errors or not.
     * @return a LocalDateTime corresponding to the decoded timestamp.
     *
     * Bit layout:
     * [  YY   ][ MM ][ DD  ][ 24H ][ 60M ][60S/2]
     * [0000000][0000][00000][00000][00000][00000]
     */
    public static LocalDateTime parseDateTimeCompact(int timestamp, boolean silent) {
        LocalDate epoch = MiMovilidadParser.EPOCH_DATE_TIME_COMPACT;

        int date = (timestamp >>> 16) & 0xFFFF;
        int time = timestamp & 0xFFFF;

        /* ---- DateCompact ---- */
        int year = (date >> 9) + epoch.getYear();
        int month = ((date >> 5) & 0xF);
        int day = (date & 0x1F);

        /* ---- TimeCompact ---- */
        int hour = (time >> 11 & 0x1F);
        int minute = (time >> 5 & 0x3F);
        int second = (time & 0x1F) * 2;

        try {
            return LocalDateTime.of(year, month, day, hour, minute, second);
        } catch (DateTimeException e) {
            if (!silent)
                Log.e("PARSE_DATETIME_COMPACT",
                        String.format("Error parsing datetime (%s): %s",
                                to32(timestamp),
                                Arrays.toString(new int[]{year, month, day, hour, minute, second})),
                        e
                );
            return null;
        }
    }

    public static int toDateTimeCompact(LocalDateTime dateTime) {
        LocalDate epoch = MiMovilidadParser.EPOCH_DATE_TIME_COMPACT;

        int year = dateTime.getYear() - epoch.getYear();
        int month = dateTime.getMonthValue();
        int day = dateTime.getDayOfMonth();

        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();
        int second = (dateTime.getSecond() + 1) / 2;

        int timestamp = 0;

        /* ---- DateCompact ---- */
        timestamp |= (year & 0x7F) << 25;
        timestamp |= (month & 0xF) << 21;
        timestamp |= (day & 0x1F) << 16;

        /* ---- TimeCompact ---- */
        timestamp |= (hour & 0x1F) << 11;
        timestamp |= (minute & 0x3F) << 5;
        timestamp |= ((second / 2) & 0x1F);

        return timestamp;
    }

    public static int parseBcd(int bcd) {
        return Integer.parseInt(Integer.toHexString(bcd));
    }

    public static LocalDate parseBcdDate(int n) {
        //  BCD “YYYYMMDD”
        // We will read four bytes in sequence: b0, b1, b2, b3.
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(n);
        buffer.position(0);
        byte b0 = buffer.get(); // n[0]
        byte b1 = buffer.get(); // n[1]
        byte b2 = buffer.get(); // n[2]
        byte b3 = buffer.get(); // n[3]

        // Decode BCD:
        int year = ((b0 & 0xF0) >> 4) * 1000
                + ((b0 & 0x0F)) * 100
                + ((b1 & 0xF0) >> 4) * 10
                + (b1 & 0x0F);

        int month = ((b2 & 0xF0) >> 4) * 10
                + (b2 & 0x0F);

        int day = ((b3 & 0xF0) >> 4) * 10
                + (b3 & 0x0F);

        if (year > 0 && month > 0 && day > 0)
            return LocalDate.of(year, month, day);
        else
            return null;
    }

    public static String to32(int timestamp) {
        String string = String.format("%32s", Integer.toBinaryString(timestamp)).replace(" ", "0");
        return String.format("%s %s %s %s %s %s",
                string.substring(0, 7),
                string.substring(7, 11),
                string.substring(11, 16),
                string.substring(16, 21),
                string.substring(21, 27),
                string.substring(27)
        );
    }
}
