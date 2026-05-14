package org.nodocentral.miviaje;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
}
