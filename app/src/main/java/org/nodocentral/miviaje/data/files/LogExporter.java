package org.nodocentral.miviaje.data.files;

import android.app.Activity;
import android.os.Process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Locale;
import java.util.function.Consumer;

public class LogExporter {

    public interface Callback {
        void onSuccess();

        void onCaptureError(Exception error);

        void onSaveError(Exception error);

        void onClearError(Exception error);
    }

    private final Activity activity;
    private final FilePicker filePicker;
    private final Consumer<Runnable> backgroundRunner;

    public LogExporter(Activity activity, FilePicker filePicker, Consumer<Runnable> backgroundRunner) {
        this.activity = activity;
        this.filePicker = filePicker;
        this.backgroundRunner = backgroundRunner;
    }

    public void export(Callback callback) {
        backgroundRunner.accept(() -> {
            try {
                String capturedLogs = captureLogsForCurrentProcess();
                String fileName = String.format(
                        Locale.getDefault(),
                        "mi_viaje_log_%s.txt",
                        LocalDate.now()
                );

                activity.runOnUiThread(() -> filePicker.saveText(
                        capturedLogs,
                        "text/plain",
                        fileName,
                        () -> clearLogcat(callback),
                        callback::onSaveError
                ));
            } catch (Exception e) {
                activity.runOnUiThread(() -> callback.onCaptureError(e));
            }
        });
    }

    private void clearLogcat(Callback callback) {
        backgroundRunner.accept(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("logcat", "-c");
                java.lang.Process process = processBuilder.start();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new IOException("logcat -c failed with exit code " + exitCode);
                }
                activity.runOnUiThread(callback::onSuccess);
            } catch (Exception e) {
                activity.runOnUiThread(() -> callback.onClearError(e));
            }
        });
    }

    private static String captureLogsForCurrentProcess() throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "logcat",
                "--pid",
                String.valueOf(Process.myPid()),
                "-d",
                "-v",
                "time"
        );
        java.lang.Process process = processBuilder.start();

        String stdout = readStream(process.getInputStream());
        String stderr = readStream(process.getErrorStream());
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("logcat failed: " + stderr);
        }

        return stdout;
    }

    private static String readStream(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        }
        return builder.toString();
    }
}
