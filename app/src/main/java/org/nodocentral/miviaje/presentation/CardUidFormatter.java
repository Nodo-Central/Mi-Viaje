package org.nodocentral.miviaje.presentation;

import android.content.SharedPreferences;

public final class CardUidFormatter {
    public static final String KEY_HIDE_CARD_UID = "setting_hide_card_uid";
    public static final boolean DEFAULT_HIDE_CARD_UID = true;

    private static final int VISIBLE_UID_CHARS = 4;
    private static final String HIDDEN_UID_PREFIX = "\u2022\u2022\u2022\u2022";

    private CardUidFormatter() {
    }

    public static boolean shouldHideCardUid(SharedPreferences preferences) {
        return preferences.getBoolean(KEY_HIDE_CARD_UID, DEFAULT_HIDE_CARD_UID);
    }

    public static String formatUid(String uid, boolean hideCardUid) {
        if (!hideCardUid || uid == null || uid.length() <= VISIBLE_UID_CHARS) {
            return uid;
        }
        return HIDDEN_UID_PREFIX + uid.substring(uid.length() - VISIBLE_UID_CHARS);
    }
}
