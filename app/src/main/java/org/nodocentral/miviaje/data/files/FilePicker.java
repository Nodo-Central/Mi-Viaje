package org.nodocentral.miviaje.data.files;

import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.util.Consumer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class FilePicker {
    private static final String TAG = "FilePicker";

    private final ComponentActivity activity;

    // Estado “pendiente” para cuando vuelva el resultado del picker
    private String pendingContent;
    private String pendingMimeType;
    private String pendingFileName;
    private Runnable onSaved;
    private Consumer<Exception> onSaveError;
    private Consumer<String> onLoaded;
    private Consumer<Exception> onError;

    private final ActivityResultLauncher<String> createJsonLauncher;
    private final ActivityResultLauncher<String> createTextLauncher;
    private final ActivityResultLauncher<String[]> openJsonLauncher;

    public FilePicker(ComponentActivity activity) {
        this.activity = activity;
        this.pendingContent = null;
        this.pendingMimeType = null;
        this.pendingFileName = null;
        this.onSaved = null;
        this.onSaveError = null;

        // Contrato: CreateDocument devuelve un Uri (o null si cancelan)
        this.createJsonLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/json"),
                this::writePendingContentToUri
        );
        this.createTextLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.CreateDocument("text/plain"),
                this::writePendingContentToUri
        );
        this.openJsonLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) return;

                    try (InputStream is = activity.getContentResolver().openInputStream(uri);
                            InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                            BufferedReader reader = new BufferedReader(isr)) {
                        StringBuilder builder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null)
                            builder.append(line);
                        onLoaded.accept(builder.toString());

                    } catch (Exception e) {
                        onError.accept(e);
                    }
                }
        );
    }

    public void loadJson(Consumer<String> onLoaded, Consumer<Exception> onError) {
        this.onLoaded = onLoaded;
        this.onError = onError;
        openJsonLauncher.launch(new String[]{"application/json", "text/*"});
    }

    public void saveJson(String json, String suggestedFileName) {
        saveText(json, "application/json", suggestedFileName, null, null);
    }

    public void saveText(String content,
                         String mimeType,
                         String suggestedFileName,
                         Runnable onSaved,
                         Consumer<Exception> onSaveError) {
        this.pendingContent = content == null ? "" : content;
        this.pendingMimeType = (mimeType == null || mimeType.isBlank()) ? "text/plain" : mimeType;
        this.pendingFileName = (suggestedFileName == null || suggestedFileName.isEmpty())
                ? "export.txt"
                : suggestedFileName;
        this.onSaved = onSaved;
        this.onSaveError = onSaveError;

        if ("application/json".equals(this.pendingMimeType)) {
            createJsonLauncher.launch(this.pendingFileName);
            return;
        }
        createTextLauncher.launch(this.pendingFileName);
    }

    private void writePendingContentToUri(android.net.Uri uri) {
        if (uri == null) return; // usuario canceló
        if (pendingContent == null || pendingMimeType == null) return;

        try (OutputStream os = activity.getContentResolver().openOutputStream(uri, "w")) {
            if (os == null) {
                throw new IllegalStateException("No se pudo abrir el archivo para escritura");
            }
            os.write(pendingContent.getBytes(StandardCharsets.UTF_8));
            os.flush();
            if (onSaved != null) {
                onSaved.run();
            }
        } catch (Exception e) {
            if (onSaveError != null) {
                onSaveError.accept(e);
            } else {
                Log.w(TAG, "Error saving file", e);
            }
        } finally {
            pendingContent = null;
            pendingMimeType = null;
            pendingFileName = null;
            onSaved = null;
            onSaveError = null;
        }
    }
}
