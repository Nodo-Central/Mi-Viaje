package org.nodocentral.miviaje.presentation;

import androidx.annotation.StyleRes;

import org.nodocentral.miviaje.R;

enum AppColorTheme {
    SYSTEM("system", R.string.system_default, 0),
    METRO_L1("metro_l1", R.string.color_theme_metro_l1, R.style.Theme_MiViaje_Palette_MetroL1),
    METRO_L2("metro_l2", R.string.color_theme_metro_l2, R.style.Theme_MiViaje_Palette_MetroL2),
    METRO_L3("metro_l3", R.string.color_theme_metro_l3, R.style.Theme_MiViaje_Palette_MetroL3),
    METRO_L4("metro_l4", R.string.color_theme_metro_l4, R.style.Theme_MiViaje_Palette_MetroL4),
    MACRO_AEROPUERTO(
            "macro_aeropuerto",
            R.string.color_theme_macro_aeropuerto,
            R.style.Theme_MiViaje_Palette_MacroAeropuerto
    ),
    MACRO_CALZADA(
            "macro_calzada",
            R.string.color_theme_macro_calzada,
            R.style.Theme_MiViaje_Palette_MacroCalzada
    ),
    MACRO_PERIFERICO(
            "macro_periferico",
            R.string.color_theme_macro_periferico,
            R.style.Theme_MiViaje_Palette_MacroPeriferico
    ),
    MI_TRANSPORTE(
            "mi_transporte",
            R.string.color_theme_mi_transporte,
            R.style.Theme_MiViaje_Palette_MiTransporte
    ),
    CORPORATE(
            "corporate",
            R.string.color_theme_corporate,
            R.style.Theme_MiViaje_Palette_Corporate
    );

    static final String PREF_KEY = "setting_color_theme";
    static final String DEFAULT_VALUE = "system";

    final String preferenceValue;
    final int labelResId;
    @StyleRes
    final int themeResId;

    AppColorTheme(String preferenceValue, int labelResId, @StyleRes int themeResId) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
        this.themeResId = themeResId;
    }

    static AppColorTheme fromPreferenceValue(String preferenceValue) {
        for (AppColorTheme colorTheme : values()) {
            if (colorTheme.preferenceValue.equals(preferenceValue)) {
                return colorTheme;
            }
        }
        return SYSTEM;
    }

    boolean appliesThemeOverride() {
        return themeResId != 0;
    }
}
