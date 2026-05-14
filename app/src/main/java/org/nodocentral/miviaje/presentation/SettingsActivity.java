package org.nodocentral.miviaje.presentation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.UiModeManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.databinding.ActivitySettingsBinding;
import org.nodocentral.miviaje.presentation.adapters.SettingsAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends BaseActivity implements SettingsAdapter.Listener {
    private static final String NIGHT_MODE_LIGHT = "light";
    private static final String NIGHT_MODE_DARK = "dark";
    private static final String NIGHT_MODE_SYSTEM = "system";
    private static final String KEY_SHOW_TECHNICAL_DATA = "setting_show_technical_data";
    private static final String KEY_SHOW_DEBUG_DATA = "setting_show_debug_data";
    private static final String KEY_REBEL_MODE = "setting_rebel_mode";
    private static final String KEY_SETTINGS_THEME_FADE_IN = "settings_theme_fade_in";
    private static final long CONTROL_POP_DELAY_MS = 180L;
    private static final long THEME_FADE_OUT_DURATION_MS = 140L;
    private static final long THEME_FADE_IN_DURATION_MS = 180L;

    private enum AppLanguage {
        SYSTEM("", R.string.system_default),
        SPANISH("es-MX", R.string.app_language_spanish),
        ENGLISH("en", R.string.app_language_english),
        JAPANESE("ja", R.string.app_language_japanese),
        KOREAN("ko", R.string.app_language_korean),
        CHINESE("zh", R.string.app_language_chinese);

        final String languageTag;
        final int labelResId;

        AppLanguage(String languageTag, int labelResId) {
            this.languageTag = languageTag;
            this.labelResId = labelResId;
        }

        static AppLanguage fromLanguageTag(String languageTag) {
            if (languageTag == null || languageTag.isEmpty()) {
                return SYSTEM;
            }

            String language = Locale.forLanguageTag(languageTag).getLanguage();
            switch (language) {
                case "es":
                    return SPANISH;
                case "en":
                    return ENGLISH;
                case "ja":
                    return JAPANESE;
                case "ko":
                    return KOREAN;
                case "zh":
                    return CHINESE;
                default:
                    return SYSTEM;
            }
        }
    }

    private SharedPreferences settingsPreferences;
    private ActivitySettingsBinding binding;
    private SettingsAdapter settingsAdapter;
    private Runnable pendingNightModeChange;
    private Runnable pendingPureDarkChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        animateInAfterThemeChangeIfNeeded();
        super.setToolbar(true);

        settingsAdapter = new SettingsAdapter(this);
        binding.settingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.settingsRecycler.setAdapter(settingsAdapter);
        refreshSettingsItems();
        applyInsetsToPadding(
                binding.settingsRecycler,
                WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.displayCutout(),
                false,
                false,
                false,
                true
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        cancelPendingSettingChanges();
        super.onDestroy();
    }

    private void refreshSettingsItems() {
        String nightMode = settingsPreferences.getString(KEY_DARK_MODE, NIGHT_MODE_SYSTEM);
        List<SettingsAdapter.Item> items = new ArrayList<>();
        items.add(new SettingsAdapter.HeaderItem(R.string.settings_customization, R.drawable.ic_palette));
        items.add(new SettingsAdapter.ThemeItem(
                nightMode,
                R.drawable.ic_light_mode_small,
                R.drawable.ic_dark_mode_small,
                R.drawable.ic_brightness_small,
                true,
                false
        ));
        items.add(new SettingsAdapter.SwitchItem(
                SettingsAdapter.SwitchKey.PURE_DARK,
                R.string.setting_pure_dark,
                0,
                R.drawable.ic_contrast,
                settingsPreferences.getBoolean(KEY_PURE_DARK, false),
                false,
                false
        ));
        items.add(new SettingsAdapter.ChoiceItem(
                SettingsAdapter.ChoiceKey.COLOR_THEME,
                R.string.setting_color_theme,
                getSelectedColorTheme().labelResId,
                R.drawable.ic_palette,
                false,
                false
        ));
        items.add(new SettingsAdapter.ChoiceItem(
                SettingsAdapter.ChoiceKey.LANGUAGE,
                R.string.setting_app_language,
                getSelectedAppLanguage().labelResId,
                R.drawable.ic_language,
                false,
                false
        ));
        items.add(new SettingsAdapter.SwitchItem(
                SettingsAdapter.SwitchKey.REBEL_MODE,
                R.string.setting_rebel_mode,
                R.string.setting_rebel_mode_summary,
                R.drawable.ic_bloom,
                settingsPreferences.getBoolean(KEY_REBEL_MODE, false),
                false,
                false
        ));
        items.add(new SettingsAdapter.SwitchItem(
                SettingsAdapter.SwitchKey.ADVANCED_DATA,
                R.string.setting_show_more_data,
                R.string.setting_show_more_data_summary,
                R.drawable.ic_graph,
                settingsPreferences.getBoolean(KEY_SHOW_TECHNICAL_DATA, false),
                false,
                false
        ));
        items.add(new SettingsAdapter.SwitchItem(
                SettingsAdapter.SwitchKey.TECHNICAL_DATA,
                R.string.setting_show_technical_data,
                R.string.setting_show_technical_data_summary,
                R.drawable.ic_debug,
                settingsPreferences.getBoolean(KEY_SHOW_DEBUG_DATA, false),
                false,
                true
        ));
        settingsAdapter.submitList(items);
    }

    @Override
    public void onThemeSelected(String nightMode) {
        String currentNightMode = settingsPreferences.getString(KEY_DARK_MODE, NIGHT_MODE_SYSTEM);
        settingsAdapter.updateThemeSelection(nightMode);
        if (!nightMode.equals(currentNightMode)) {
            scheduleNightModeChange(nightMode);
        } else {
            cancelPendingNightModeChange();
        }
    }

    @Override
    public void onChoiceClicked(SettingsAdapter.ChoiceKey key) {
        switch (key) {
            case COLOR_THEME:
                showColorThemeDialog();
                break;
            case LANGUAGE:
                showLanguageDialog();
                break;
        }
    }

    @Override
    public void onSwitchChanged(SettingsAdapter.SwitchKey key, boolean checked) {
        String preferenceKey = getPreferenceKey(key);
        settingsAdapter.updateSwitchChecked(key, checked);
        if (settingsPreferences.getBoolean(preferenceKey, false) != checked) {
            if (key == SettingsAdapter.SwitchKey.PURE_DARK) {
                schedulePureDarkChange(checked);
            } else {
                settingsPreferences.edit().putBoolean(preferenceKey, checked).apply();
            }
        } else if (key == SettingsAdapter.SwitchKey.PURE_DARK) {
            cancelPendingPureDarkChange();
        }
    }

    private void scheduleNightModeChange(String nightMode) {
        cancelPendingNightModeChange();
        String currentNightMode = settingsPreferences.getString(KEY_DARK_MODE, NIGHT_MODE_SYSTEM);
        boolean destinationDark = isEffectiveDarkTheme(nightMode);
        boolean shouldFade = isEffectiveDarkTheme(currentNightMode) != destinationDark;
        pendingNightModeChange = () -> {
            Runnable changeTheme = () -> applyNightModePreference(nightMode, shouldFade);
            if (shouldFade) {
                fadeIntoThemeChange(destinationDark ? Color.BLACK : Color.WHITE, changeTheme);
            } else {
                changeTheme.run();
            }
            pendingNightModeChange = null;
        };
        binding.settingsRecycler.postDelayed(pendingNightModeChange, CONTROL_POP_DELAY_MS);
    }

    private void applyNightModePreference(String nightMode, boolean effectiveThemeChanges) {
        settingsPreferences.edit().putString(KEY_DARK_MODE, nightMode).apply();
        if (effectiveThemeChanges && getSelectedColorTheme().appliesThemeOverride()) {
            binding.settingsRoot.post(this::recreate);
        }
    }

    private void schedulePureDarkChange(boolean checked) {
        cancelPendingPureDarkChange();
        boolean shouldFade = isEffectiveDarkTheme(settingsPreferences.getString(KEY_DARK_MODE, NIGHT_MODE_SYSTEM));
        pendingPureDarkChange = () -> {
            Runnable changeTheme = () -> settingsPreferences.edit().putBoolean(KEY_PURE_DARK, checked).apply();
            if (shouldFade) {
                fadeIntoThemeChange(Color.BLACK, changeTheme);
            } else {
                changeTheme.run();
            }
            pendingPureDarkChange = null;
        };
        binding.settingsRecycler.postDelayed(pendingPureDarkChange, CONTROL_POP_DELAY_MS);
    }

    private void fadeIntoThemeChange(int overlayColor, Runnable changeTheme) {
        ViewGroup content = findViewById(android.R.id.content);
        View overlay = new View(this);
        overlay.setBackgroundColor(overlayColor);
        overlay.setAlpha(0f);
        content.addView(overlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        overlay.animate()
                .alpha(1f)
                .setDuration(THEME_FADE_OUT_DURATION_MS)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        settingsPreferences.edit().putBoolean(KEY_SETTINGS_THEME_FADE_IN, true).apply();
                        changeTheme.run();
                        overlay.postDelayed(() -> fadeOutUnusedThemeOverlay(overlay), THEME_FADE_IN_DURATION_MS);
                    }
                })
                .start();
    }

    private boolean isEffectiveDarkTheme(String nightMode) {
        if (NIGHT_MODE_DARK.equals(nightMode)) {
            return true;
        }
        if (NIGHT_MODE_LIGHT.equals(nightMode)) {
            return false;
        }
        return isSystemDarkTheme();
    }

    private boolean isSystemDarkTheme() {
        UiModeManager uiModeManager = getSystemService(UiModeManager.class);
        if (uiModeManager != null) {
            int systemNightMode = uiModeManager.getNightMode();
            if (systemNightMode == UiModeManager.MODE_NIGHT_YES) {
                return true;
            }
            if (systemNightMode == UiModeManager.MODE_NIGHT_NO) {
                return false;
            }
        }
        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    private void fadeOutUnusedThemeOverlay(View overlay) {
        if (!overlay.isAttachedToWindow()) {
            return;
        }
        settingsPreferences.edit().remove(KEY_SETTINGS_THEME_FADE_IN).apply();
        overlay.animate()
                .alpha(0f)
                .setDuration(THEME_FADE_IN_DURATION_MS)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ViewGroup parent = (ViewGroup) overlay.getParent();
                        if (parent != null) {
                            parent.removeView(overlay);
                        }
                    }
                })
                .start();
    }

    private void animateInAfterThemeChangeIfNeeded() {
        if (!settingsPreferences.getBoolean(KEY_SETTINGS_THEME_FADE_IN, false)) {
            return;
        }
        settingsPreferences.edit().remove(KEY_SETTINGS_THEME_FADE_IN).apply();
        binding.settingsRoot.setAlpha(0f);
        binding.settingsRoot.post(() -> binding.settingsRoot.animate()
                .alpha(1f)
                .setDuration(THEME_FADE_IN_DURATION_MS)
                .setListener(null)
                .start());
    }

    private void cancelPendingNightModeChange() {
        if (pendingNightModeChange != null) {
            binding.settingsRecycler.removeCallbacks(pendingNightModeChange);
            pendingNightModeChange = null;
        }
    }

    private void cancelPendingPureDarkChange() {
        if (pendingPureDarkChange != null) {
            binding.settingsRecycler.removeCallbacks(pendingPureDarkChange);
            pendingPureDarkChange = null;
        }
    }

    private void cancelPendingSettingChanges() {
        if (binding == null) {
            return;
        }
        cancelPendingNightModeChange();
        cancelPendingPureDarkChange();
    }

    private void showLanguageDialog() {
        AppLanguage[] languages = AppLanguage.values();
        CharSequence[] labels = new CharSequence[languages.length];
        for (int i = 0; i < languages.length; i++) {
            labels[i] = getString(languages[i].labelResId);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.setting_app_language)
                .setSingleChoiceItems(labels, getSelectedAppLanguage().ordinal(), (dialog, which) -> {
                    setAppLanguage(languages[which]);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showColorThemeDialog() {
        AppColorTheme[] colorThemes = AppColorTheme.values();
        CharSequence[] labels = new CharSequence[colorThemes.length];
        for (int i = 0; i < colorThemes.length; i++) {
            labels[i] = getString(colorThemes[i].labelResId);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.setting_color_theme)
                .setSingleChoiceItems(labels, getSelectedColorTheme().ordinal(), (dialog, which) -> {
                    setColorTheme(colorThemes[which]);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setColorTheme(AppColorTheme colorTheme) {
        String currentColorTheme = settingsPreferences.getString(
                AppColorTheme.PREF_KEY,
                AppColorTheme.DEFAULT_VALUE
        );
        if (!colorTheme.preferenceValue.equals(currentColorTheme)) {
            settingsPreferences.edit().putString(AppColorTheme.PREF_KEY, colorTheme.preferenceValue).apply();
        } else {
            refreshSettingsItems();
        }
    }

    private AppColorTheme getSelectedColorTheme() {
        return AppColorTheme.fromPreferenceValue(
                settingsPreferences.getString(AppColorTheme.PREF_KEY, AppColorTheme.DEFAULT_VALUE)
        );
    }

    private void setAppLanguage(AppLanguage language) {
        LocaleListCompat locales = AppLanguage.SYSTEM == language
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(language.languageTag);
        if (!locales.equals(AppCompatDelegate.getApplicationLocales())) {
            AppCompatDelegate.setApplicationLocales(locales);
        } else {
            refreshSettingsItems();
        }
    }

    private AppLanguage getSelectedAppLanguage() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (locales.isEmpty() || locales.get(0) == null) {
            return AppLanguage.SYSTEM;
        }
        return AppLanguage.fromLanguageTag(locales.get(0).toLanguageTag());
    }

    private String getPreferenceKey(SettingsAdapter.SwitchKey key) {
        switch (key) {
            case PURE_DARK:
                return KEY_PURE_DARK;
            case ADVANCED_DATA:
                return KEY_SHOW_TECHNICAL_DATA;
            case TECHNICAL_DATA:
                return KEY_SHOW_DEBUG_DATA;
            case REBEL_MODE:
                return KEY_REBEL_MODE;
            default:
                throw new IllegalArgumentException("Unknown switch key: " + key);
        }
    }
}
