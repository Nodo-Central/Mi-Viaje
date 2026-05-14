package org.nodocentral.miviaje.data.files;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;

import org.nodocentral.miviaje.data.artwork.CardArtworkResolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Locale;

public class ArtworkImageManager {
    private static final String TAG = "ArtworkImageManager";

    public static final int MAX_ARTWORK_SIDE_PX = 2048;
    private static final int ARTWORK_JPEG_QUALITY = 90;

    private final Context appContext;

    public ArtworkImageManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public ImportedImageData importFromUri(Uri sourceUri, String artworkId) throws Exception {
        Bitmap bitmap = loadNormalizedBitmapFromUri(sourceUri);
        ImageFormat format = chooseImageFormat(bitmap);
        File targetFile = new File(requireArtworksDirectory(), artworkId + format.extension);
        try {
            String sha256 = writeBitmapToFileWithSha256(bitmap, targetFile, format);
            return new ImportedImageData(targetFile.getName(), format.mimeType, sha256);
        } catch (Exception e) {
            deleteFileQuietly(targetFile);
            throw e;
        } finally {
            bitmap.recycle();
        }
    }

    public ImportedImageData normalizeExistingFile(String artworkId, File currentFile) throws Exception {
        Bitmap bitmap = decodeScaledBitmapFromFile(currentFile);
        ImageFormat format = chooseImageFormat(bitmap);
        String finalFileName = artworkId + format.extension;
        File artworksDir = requireArtworksDirectory();
        File targetFile = new File(artworksDir, finalFileName);
        File tempFile = new File(artworksDir, artworkId + ".tmp");

        try {
            String sha256 = writeBitmapToFileWithSha256(bitmap, tempFile, format);
            if (targetFile.exists() && !targetFile.equals(tempFile)) {
                deleteFileQuietly(targetFile);
            }
            if (!tempFile.renameTo(targetFile)) {
                throw new IllegalStateException("Could not replace normalized artwork file");
            }
            if (!currentFile.equals(targetFile)) {
                deleteFileQuietly(currentFile);
            }
            return new ImportedImageData(finalFileName, format.mimeType, sha256);
        } finally {
            deleteFileQuietly(tempFile);
            bitmap.recycle();
        }
    }

    public ImageDimensions readDimensions(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        if (options.outWidth <= 0 || options.outHeight <= 0) {
            return null;
        }
        return new ImageDimensions(options.outWidth, options.outHeight);
    }

    public String queryDisplayName(Uri uri) {
        try (Cursor cursor = appContext.getContentResolver().query(
                uri,
                new String[]{OpenableColumns.DISPLAY_NAME},
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (displayNameIndex >= 0) {
                    return cursor.getString(displayNameIndex);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not query display name", e);
        }
        return null;
    }

    public File requireArtworksDirectory() {
        File artworksDir = new File(appContext.getFilesDir(), CardArtworkResolver.ARTWORKS_DIR);
        if (!artworksDir.exists() && !artworksDir.mkdirs()) {
            throw new IllegalStateException("Could not create artwork dir");
        }
        return artworksDir;
    }

    public void deleteByRelativePath(String relativePath) {
        if (relativePath == null) {
            return;
        }
        File file = new File(requireArtworksDirectory(), relativePath);
        deleteFileQuietly(file);
    }

    private Bitmap decodeScaledBitmapFromFile(File file) throws Exception {
        ImageDimensions dimensions = readDimensions(file);
        if (dimensions == null) {
            throw new InvalidImageException("Artwork file has invalid dimensions");
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(
                dimensions.width,
                dimensions.height,
                MAX_ARTWORK_SIDE_PX
        );
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap decoded = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        if (decoded == null || decoded.getWidth() <= 0 || decoded.getHeight() <= 0) {
            throw new InvalidImageException("Artwork file could not be decoded");
        }

        Bitmap scaled = scaleBitmapIfNeeded(decoded, MAX_ARTWORK_SIDE_PX);
        if (scaled != decoded) {
            decoded.recycle();
        }
        return scaled;
    }

    private Bitmap loadNormalizedBitmapFromUri(Uri sourceUri) throws Exception {
        FutureTarget<Bitmap> futureTarget = Glide.with(appContext)
                .asBitmap()
                .load(sourceUri)
                .apply(new RequestOptions()
                        .override(MAX_ARTWORK_SIDE_PX, MAX_ARTWORK_SIDE_PX)
                        .downsample(DownsampleStrategy.AT_MOST)
                        .disallowHardwareConfig())
                .submit();

        try {
            Bitmap loaded = futureTarget.get();
            if (loaded == null || loaded.getWidth() <= 0 || loaded.getHeight() <= 0) {
                throw new InvalidImageException("Invalid or unsupported image");
            }

            Bitmap scaled = scaleBitmapIfNeeded(loaded, MAX_ARTWORK_SIDE_PX);
            if (scaled != loaded) {
                loaded.recycle();
            }
            return scaled;
        } catch (OutOfMemoryError e) {
            throw new InvalidImageException("Image is too large to process", e);
        } catch (InvalidImageException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidImageException("Could not decode image", e);
        } finally {
            Glide.with(appContext).clear(futureTarget);
        }
    }

    private static Bitmap scaleBitmapIfNeeded(Bitmap bitmap, int maxSidePx) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int longestSide = Math.max(width, height);
        if (longestSide <= maxSidePx) {
            return bitmap;
        }
        float scale = (float) maxSidePx / (float) longestSide;
        int targetWidth = Math.max(1, Math.round(width * scale));
        int targetHeight = Math.max(1, Math.round(height * scale));
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
    }

    private static ImageFormat chooseImageFormat(Bitmap bitmap) {
        if (bitmap.hasAlpha()) {
            return new ImageFormat(Bitmap.CompressFormat.PNG, ".png", "image/png");
        }
        return new ImageFormat(Bitmap.CompressFormat.JPEG, ".jpg", "image/jpeg");
    }

    private String writeBitmapToFileWithSha256(Bitmap bitmap, File targetFile, ImageFormat format)
            throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (OutputStream fileOutputStream = new FileOutputStream(targetFile);
             DigestOutputStream digestOutputStream = new DigestOutputStream(fileOutputStream, digest)) {
            boolean success = bitmap.compress(
                    format.compressFormat,
                    format.compressFormat == Bitmap.CompressFormat.JPEG ? ARTWORK_JPEG_QUALITY : 100,
                    digestOutputStream
            );
            if (!success) {
                throw new InvalidImageException("Could not encode normalized artwork");
            }
            digestOutputStream.flush();
        }
        return toSha256Hex(digest.digest());
    }

    private static int calculateInSampleSize(int width, int height, int maxSidePx) {
        int inSampleSize = 1;
        while ((width / inSampleSize) > maxSidePx || (height / inSampleSize) > maxSidePx) {
            inSampleSize *= 2;
        }
        return Math.max(inSampleSize, 1);
    }

    private static String toSha256Hex(byte[] hash) {
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            hex.append(String.format(Locale.US, "%02x", b));
        }
        return hex.toString();
    }

    private static void deleteFileQuietly(File file) {
        if (file != null && file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    public static final class ImportedImageData {
        public final String fileName;
        public final String mimeType;
        public final String sha256;

        public ImportedImageData(String fileName, String mimeType, String sha256) {
            this.fileName = fileName;
            this.mimeType = mimeType;
            this.sha256 = sha256;
        }
    }

    public static final class ImageDimensions {
        public final int width;
        public final int height;

        public ImageDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public boolean exceedsMaxSide(int maxSidePx) {
            return width > maxSidePx || height > maxSidePx;
        }
    }

    public static class InvalidImageException extends Exception {
        public InvalidImageException(String message) {
            super(message);
        }

        public InvalidImageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final class ImageFormat {
        final Bitmap.CompressFormat compressFormat;
        final String extension;
        final String mimeType;

        ImageFormat(Bitmap.CompressFormat compressFormat, String extension, String mimeType) {
            this.compressFormat = compressFormat;
            this.extension = extension;
            this.mimeType = mimeType;
        }
    }
}
