package org.nodocentral.miviaje.data.artwork;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.domain.artwork.Artwork;
import org.nodocentral.miviaje.domain.mimovilidad.card.Card;
import org.nodocentral.miviaje.domain.mimovilidad.card.Product.State;
import org.nodocentral.miviaje.domain.mimovilidad.card.User.Profile.Type;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

public final class CardArtworkResolver {
    private static final String TAG = "CardArtworkResolver";
    private static final int MAX_CUSTOM_ARTWORK_SIDE_PX = 2048;
    private static final int BRANDING_SHIFT_YEAR = 2025;
    private static final int BRANDING_SHIFT_YEAR_2 = 2026;

    public static final String PREFIX_BUILTIN = "builtin:";
    public static final String PREFIX_ARTWORK = "artwork:";
    public static final String ARTWORKS_DIR = "artworks";

    public static final class BuiltinOption {
        public final String key;
        @DrawableRes public final int drawableResId;
        @StringRes public final int labelResId;

        public BuiltinOption(String key, int drawableResId, int labelResId) {
            this.key = key;
            this.drawableResId = drawableResId;
            this.labelResId = labelResId;
        }
    }

    private static final List<BuiltinOption> BUILTIN_OPTIONS = new ArrayList<>();
    static {
        BUILTIN_OPTIONS.add(new BuiltinOption("card_2020_general", R.drawable.card_2020_general, R.string.artwork_builtin_2020_general));
        BUILTIN_OPTIONS.add(new BuiltinOption("card_2020_subsidy_50", R.drawable.card_2020_subsidy_50, R.string.artwork_builtin_2020_subsidy_50));
        BUILTIN_OPTIONS.add(new BuiltinOption("card_2020_subsidy_100", R.drawable.card_2020_subsidy_100, R.string.artwork_builtin_2020_subsidy_100));
        BUILTIN_OPTIONS.add(new BuiltinOption("card_2025_general", R.drawable.card_2025_general, R.string.artwork_builtin_2025_general));
        BUILTIN_OPTIONS.add(new BuiltinOption("card_2025_subsidy_50", R.drawable.card_2025_subsidy_50, R.string.artwork_builtin_2025_subsidy_50));
        BUILTIN_OPTIONS.add(new BuiltinOption("card_2025_subsidy_100", R.drawable.card_2025_subsidy_100, R.string.artwork_builtin_2025_subsidy_100));
        BUILTIN_OPTIONS.add(new BuiltinOption("card_2026_single_card", R.drawable.card_2026_single_card, R.string.artwork_builtin_2026_social));
    }

    private CardArtworkResolver() {
    }

    public static List<BuiltinOption> getBuiltinOptions() {
        return new ArrayList<>(BUILTIN_OPTIONS);
    }

    public static String toBuiltinRef(String key) {
        return PREFIX_BUILTIN + key;
    }

    public static String toArtworkRef(String artworkId) {
        return PREFIX_ARTWORK + artworkId;
    }

    public static String getArtworkId(String artworkRef) {
        if (artworkRef == null || !artworkRef.startsWith(PREFIX_ARTWORK)) {
            return null;
        }
        return artworkRef.substring(PREFIX_ARTWORK.length());
    }

    @DrawableRes
    public static int resolveAutomaticBackground(Card card) {
        Type profileType = card.getUser().getProfile().getType();
        int productionYear = getProductionYear(card.getProductionDate());

        if (profileType == Type.SINGLE_CARD) {
            return R.drawable.card_2026_single_card;
        }
        if (profileType != Type.GENERAL_FARE) {
            return resolveSubsidizedBackground(card, productionYear);
        }
        return productionYear >= BRANDING_SHIFT_YEAR
                ? R.drawable.card_2025_general
                : R.drawable.card_2020_general;
    }

    public static boolean applyCustomBackground(Context context,
                                                String artworkRef,
                                                Artwork artwork,
                                                android.view.View targetView) {
        if (artworkRef == null || artworkRef.isBlank() || targetView == null) {
            return false;
        }

        if (artworkRef.startsWith(PREFIX_BUILTIN)) {
            String key = artworkRef.substring(PREFIX_BUILTIN.length());
            for (BuiltinOption option : BUILTIN_OPTIONS) {
                if (option.key.equals(key)) {
                    targetView.setBackgroundResource(option.drawableResId);
                    return true;
                }
            }
            return false;
        }

        if (artworkRef.startsWith(PREFIX_ARTWORK) && artwork != null && artwork.getRelativePath() != null) {
            File file = new File(new File(context.getFilesDir(), ARTWORKS_DIR), artwork.getRelativePath());
            Drawable drawable = decodeArtworkDrawable(context, file);
            if (drawable != null) {
                try {
                    targetView.setBackground(drawable);
                    return true;
                } catch (RuntimeException e) {
                    Log.w(TAG, "Failed to apply custom artwork background", e);
                }
            }
        }
        return false;
    }

    @DrawableRes
    private static int resolveSubsidizedBackground(Card card, int productionYear) {
        if (productionYear >= BRANDING_SHIFT_YEAR_2) {
            return R.drawable.card_2026_single_card;
        }
        if (card.getBPDState() == State.ACTIVE) {
            return productionYear >= BRANDING_SHIFT_YEAR
                    ? R.drawable.card_2025_subsidy_100
                    : R.drawable.card_2020_subsidy_100;
        }
        return productionYear >= BRANDING_SHIFT_YEAR
                ? R.drawable.card_2025_subsidy_50
                : R.drawable.card_2020_subsidy_50;
    }

    private static int getProductionYear(LocalDate productionDate) {
        if (productionDate == null) {
            return 0;
        }
        return productionDate.get(WeekFields.ISO.weekBasedYear());
    }

    private static Drawable decodeArtworkDrawable(Context context, File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }

        try {
            BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
            boundsOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), boundsOptions);
            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                return null;
            }

            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = calculateInSampleSize(
                    boundsOptions.outWidth,
                    boundsOptions.outHeight,
                    MAX_CUSTOM_ARTWORK_SIDE_PX
            );
            decodeOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap decoded = BitmapFactory.decodeFile(file.getAbsolutePath(), decodeOptions);
            if (decoded == null) {
                return null;
            }

            Bitmap scaled = scaleDownToMaxSide(decoded, MAX_CUSTOM_ARTWORK_SIDE_PX);
            if (scaled != decoded) {
                decoded.recycle();
            }
            return new BitmapDrawable(context.getResources(), scaled);
        } catch (OutOfMemoryError | RuntimeException e) {
            Log.w(TAG, "Failed to decode custom artwork drawable", e);
            return null;
        }
    }

    private static int calculateInSampleSize(int width, int height, int maxSidePx) {
        int inSampleSize = 1;
        while ((width / inSampleSize) > maxSidePx || (height / inSampleSize) > maxSidePx) {
            inSampleSize *= 2;
        }
        return Math.max(1, inSampleSize);
    }

    private static Bitmap scaleDownToMaxSide(Bitmap source, int maxSidePx) {
        if (source == null) {
            return null;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        int longestSide = Math.max(width, height);
        if (longestSide <= maxSidePx) {
            return source;
        }

        float scale = (float) maxSidePx / (float) longestSide;
        int targetWidth = Math.max(1, Math.round(width * scale));
        int targetHeight = Math.max(1, Math.round(height * scale));
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true);
    }
}
