package org.nodocentral.miviaje.presentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.nodocentral.miviaje.R;

public class AppColorThemeTest {
    @Test
    public void fromPreferenceValue_missingOrUnknownValue_returnsSystem() {
        assertEquals(AppColorTheme.SYSTEM, AppColorTheme.fromPreferenceValue(null));
        assertEquals(AppColorTheme.SYSTEM, AppColorTheme.fromPreferenceValue(""));
        assertEquals(AppColorTheme.SYSTEM, AppColorTheme.fromPreferenceValue("missing"));
    }

    @Test
    public void systemTheme_hasNoThemeOverride() {
        assertEquals(0, AppColorTheme.SYSTEM.themeResId);
        assertFalse(AppColorTheme.SYSTEM.appliesThemeOverride());
    }

    @Test
    public void transitThemes_mapToExpectedStyleResources() {
        assertTheme(AppColorTheme.METRO_L1, "metro_l1", R.style.Theme_MiViaje_Palette_MetroL1);
        assertTheme(AppColorTheme.METRO_L2, "metro_l2", R.style.Theme_MiViaje_Palette_MetroL2);
        assertTheme(AppColorTheme.METRO_L3, "metro_l3", R.style.Theme_MiViaje_Palette_MetroL3);
        assertTheme(AppColorTheme.METRO_L4, "metro_l4", R.style.Theme_MiViaje_Palette_MetroL4);
        assertTheme(
                AppColorTheme.MACRO_AEROPUERTO,
                "macro_aeropuerto",
                R.style.Theme_MiViaje_Palette_MacroAeropuerto
        );
        assertTheme(
                AppColorTheme.MACRO_CALZADA,
                "macro_calzada",
                R.style.Theme_MiViaje_Palette_MacroCalzada
        );
        assertTheme(
                AppColorTheme.MACRO_PERIFERICO,
                "macro_periferico",
                R.style.Theme_MiViaje_Palette_MacroPeriferico
        );
        assertTheme(
                AppColorTheme.MI_TRANSPORTE,
                "mi_transporte",
                R.style.Theme_MiViaje_Palette_MiTransporte
        );
        assertTheme(
                AppColorTheme.CORPORATE,
                "corporate",
                R.style.Theme_MiViaje_Palette_Corporate
        );
    }

    private void assertTheme(AppColorTheme colorTheme, String preferenceValue, int themeResId) {
        assertEquals(colorTheme, AppColorTheme.fromPreferenceValue(preferenceValue));
        assertEquals(themeResId, colorTheme.themeResId);
        assertTrue(colorTheme.appliesThemeOverride());
    }
}
