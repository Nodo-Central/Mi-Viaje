package org.nodocentral.miviaje.presentation;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.preference.PreferenceManager;

import org.nodocentral.miviaje.R;

public abstract class BaseActivity extends AppCompatActivity {
    protected static final String KEY_DARK_MODE = "night_mode";
    protected static final String KEY_PURE_DARK = "setting_pure_dark";
    private static final String NIGHT_MODE_SYSTEM = "system";
    private static final String NIGHT_MODE_LIGHT = "light";
    private static final String NIGHT_MODE_DARK = "dark";

    private static final class InitialPadding {
        final int left;
        final int top;
        final int right;
        final int bottom;

        InitialPadding(View view) {
            this.left = view.getPaddingLeft();
            this.top = view.getPaddingTop();
            this.right = view.getPaddingRight();
            this.bottom = view.getPaddingBottom();
        }
    }

    private static final class InitialMargins {
        final int left;
        final int top;
        final int right;
        final int bottom;

        InitialMargins(ViewGroup.MarginLayoutParams lp) {
            this.left = lp.leftMargin;
            this.top = lp.topMargin;
            this.right = lp.rightMargin;
            this.bottom = lp.bottomMargin;
        }
    }

    protected SharedPreferences preferences;
    protected SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private AppColorTheme appliedColorTheme;
    private boolean appliedPureDarkTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setNightMode(preferences);
        appliedColorTheme = AppColorTheme.fromPreferenceValue(
                preferences.getString(AppColorTheme.PREF_KEY, AppColorTheme.DEFAULT_VALUE)
        );
        if (appliedColorTheme.appliesThemeOverride()) {
            setTheme(appliedColorTheme.themeResId);
        }
        appliedPureDarkTheme = shouldUsePureDarkTheme(preferences);
        if (appliedPureDarkTheme) {
            getTheme().applyStyle(R.style.ThemeOverlay_MiViaje_PureDark, true);
        }
        preferenceChangeListener = (sharedPreferences, key) -> {
            if (KEY_DARK_MODE.equals(key)) {
                setNightMode(sharedPreferences);
            } else if (KEY_PURE_DARK.equals(key) || AppColorTheme.PREF_KEY.equals(key)) {
                recreate();
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppColorTheme selectedColorTheme = AppColorTheme.fromPreferenceValue(
                preferences.getString(AppColorTheme.PREF_KEY, AppColorTheme.DEFAULT_VALUE)
        );
        boolean usePureDarkTheme = shouldUsePureDarkTheme(preferences);
        if (selectedColorTheme != appliedColorTheme || usePureDarkTheme != appliedPureDarkTheme) {
            recreate();
        }
    }

    @Override
    protected void onDestroy() {
        if (preferenceChangeListener != null) {
            preferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        }
        super.onDestroy();
    }

    public int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    protected static void setNightMode(SharedPreferences sharedPreferences) {
        String nightModePreference = sharedPreferences.getString(KEY_DARK_MODE, NIGHT_MODE_SYSTEM);
        int nightMode;
        switch (nightModePreference) {
            case NIGHT_MODE_LIGHT:
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case NIGHT_MODE_DARK:
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    private boolean shouldUsePureDarkTheme(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(KEY_PURE_DARK, false) && isEffectiveDarkTheme(sharedPreferences);
    }

    private boolean isEffectiveDarkTheme(SharedPreferences sharedPreferences) {
        String nightModePreference = sharedPreferences.getString(KEY_DARK_MODE, NIGHT_MODE_SYSTEM);
        if (NIGHT_MODE_DARK.equals(nightModePreference)) {
            return true;
        }
        if (NIGHT_MODE_LIGHT.equals(nightModePreference)) {
            return false;
        }
        int nightModeFlags = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    protected ActionBar setToolbar(boolean showActivityTitle) {
        Toolbar appBar = findViewById(R.id.app_bar);
        appBar.setPopupTheme(R.style.ThemeOverlay_MiViaje_PopupMenu);
        setSupportActionBar(appBar);
        setupOverflowMenu(appBar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (showActivityTitle) {
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            } else {
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setLogo(R.drawable.app_logo_text);
            }
        }

        applySystemBars(appBar);

        return actionBar;
    }

    protected int getOverflowMenuResId() {
        return 0;
    }

    private void setupOverflowMenu(Toolbar toolbar) {
        ImageButton overflowButton = toolbar.findViewById(R.id.toolbar_overflow_button);
        if (overflowButton == null) {
            return;
        }

        int menuResId = getOverflowMenuResId();
        if (menuResId == 0) {
            overflowButton.setVisibility(View.GONE);
            return;
        }

        overflowButton.setVisibility(View.VISIBLE);
        overflowButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(
                    new ContextThemeWrapper(this, R.style.ThemeOverlay_MiViaje_PopupMenu),
                    overflowButton
            );
            popupMenu.getMenuInflater().inflate(menuResId, popupMenu.getMenu());
            popupMenu.setForceShowIcon(true);
            popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
            popupMenu.show();
        });
    }

    private void applySystemBars(Toolbar appBar) {
        WindowInsetsControllerCompat ic =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (ic != null) {
            ic.setAppearanceLightStatusBars(isLightTheme());
        }

        applyInsetsToPadding(
                appBar,
                WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout(),
                false,
                true,
                false,
                false
        );
    }

    protected void applyInsetsToPadding(View view,
                                        int insetTypes,
                                        boolean applyLeft,
                                        boolean applyTop,
                                        boolean applyRight,
                                        boolean applyBottom) {
        applyInsetsToPadding(view, insetTypes, applyLeft, applyTop, applyRight, applyBottom, 0, 0, 0, 0);
    }

    protected void applyInsetsToPadding(View view,
                                        int insetTypes,
                                        boolean applyLeft,
                                        boolean applyTop,
                                        boolean applyRight,
                                        boolean applyBottom,
                                        int extraLeft,
                                        int extraTop,
                                        int extraRight,
                                        int extraBottom) {
        if (view == null) {
            return;
        }

        InitialPadding initialPadding = new InitialPadding(view);
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets values = insets.getInsets(insetTypes);
            int left = initialPadding.left + (applyLeft ? values.left : 0) + extraLeft;
            int top = initialPadding.top + (applyTop ? values.top : 0) + extraTop;
            int right = initialPadding.right + (applyRight ? values.right : 0) + extraRight;
            int bottom = initialPadding.bottom + (applyBottom ? values.bottom : 0) + extraBottom;
            v.setPadding(left, top, right, bottom);
            return insets;
        });
        requestApplyInsetsWhenAttached(view);
    }

    protected void applyInsetsToMargins(View view,
                                        int insetTypes,
                                        boolean applyLeft,
                                        boolean applyTop,
                                        boolean applyRight,
                                        boolean applyBottom) {
        applyInsetsToMargins(view, insetTypes, applyLeft, applyTop, applyRight, applyBottom, 0, 0, 0, 0);
    }

    protected void applyInsetsToMargins(View view,
                                        int insetTypes,
                                        boolean applyLeft,
                                        boolean applyTop,
                                        boolean applyRight,
                                        boolean applyBottom,
                                        int extraLeft,
                                        int extraTop,
                                        int extraRight,
                                        int extraBottom) {
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams currentLayoutParams = view.getLayoutParams();
        if (!(currentLayoutParams instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }

        InitialMargins initialMargins = new InitialMargins((ViewGroup.MarginLayoutParams) currentLayoutParams);
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            if (!(lp instanceof ViewGroup.MarginLayoutParams)) {
                return insets;
            }

            Insets values = insets.getInsets(insetTypes);
            ViewGroup.MarginLayoutParams marginLp = (ViewGroup.MarginLayoutParams) lp;
            marginLp.leftMargin = initialMargins.left + (applyLeft ? values.left : 0) + extraLeft;
            marginLp.topMargin = initialMargins.top + (applyTop ? values.top : 0) + extraTop;
            marginLp.rightMargin = initialMargins.right + (applyRight ? values.right : 0) + extraRight;
            marginLp.bottomMargin = initialMargins.bottom + (applyBottom ? values.bottom : 0) + extraBottom;
            v.setLayoutParams(marginLp);
            return insets;
        });
        requestApplyInsetsWhenAttached(view);
    }

    private static void requestApplyInsetsWhenAttached(View view) {
        if (ViewCompat.isAttachedToWindow(view)) {
            ViewCompat.requestApplyInsets(view);
            return;
        }

        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                v.removeOnAttachStateChangeListener(this);
                ViewCompat.requestApplyInsets(v);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                // No-op
            }
        });
    }

    private boolean isLightTheme() {
        int nightModeFlags = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_NO;
    }
}
