package org.nodocentral.miviaje.presentation;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.data.backup.BackupService;
import org.nodocentral.miviaje.data.backup.models.BackupImportResult;
import org.nodocentral.miviaje.data.files.FilePicker;
import org.nodocentral.miviaje.data.files.LogExporter;
import org.nodocentral.miviaje.data.parsers.MiMovilidadParser;
import org.nodocentral.miviaje.data.repository.ArtworkRepository;
import org.nodocentral.miviaje.data.repository.CardRepository;
import org.nodocentral.miviaje.data.room.MiViajeDatabase;
import org.nodocentral.miviaje.domain.artwork.Artwork;
import org.nodocentral.miviaje.domain.mimovilidad.card.Card;
import org.nodocentral.miviaje.presentation.adapters.CardAdapter;

import java.io.InvalidObjectException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends BaseActivity implements NfcAdapter.ReaderCallback, CardScanSheetController.Listener {
    private static final String TAG_CARD_ALIAS = "CARD_ALIAS";
    private static final String TAG_CARD_SCAN = "CARD_SCAN";
    private static final String TAG_CARD_LIST = "CARD_LIST";
    private static final String TAG_CARD_EXPORT = "CARD_EXPORT";
    private static final String TAG_DATA_IMPORT = "DATA_IMPORT";
    private static final String TAG_IMPORT_DATA = "IMPORT_DATA";
    private static final String TAG_LOG_EXPORT = "LOG_EXPORT";

    private static final int FAB_LAST_ITEM_EXTRA_SPACING_DP = 12;
    private static final int NFC_READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    private static final String CARD_TRANSITION_NAME_PREFIX = "card_";
    private final Object artworkSanitizationLock = new Object();
    private final Object cardScanLock = new Object();
    private NfcAdapter nfcAdapter;
    private MiViajeDatabase database;
    private CardAdapter cardAdapter;
    private RecyclerView cardRecycler;
    private FloatingActionButton addCardButton;
    private LastItemBottomSpacingDecoration lastItemBottomSpacingDecoration;
    private CardScanSheetController scanSheetController;
    private CardListUiStateController cardListUiStateController;
    private FilePicker picker;
    private LogExporter logExporter;
    private ActivityResultLauncher<Intent> artworkPickerLauncher;
    private TaskCoordinator taskCoordinator;
    private boolean artworkSanitizationFinished = false;
    private boolean cardScanActive = false;
    private boolean cardScanInProgress = false;
    private ScanSheetState scanSheetState = ScanSheetState.WAITING;
    private Card scannedCard;
    private boolean scannedCardWasNew;
    private BackupService backupService;
    private ArtworkRepository artworkRepository;
    private CardRepository cardRepository;

    private static String normalizeAlias(String alias) {
        return normalizeOptionalText(alias);
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbar(false);

        initializeCoreComponents();
        cardListUiStateController = new CardListUiStateController(findViewById(android.R.id.content));
        setupCardRecycler();
        setupAddCardButton();
        updateRecycler();
    }

    private void initializeCoreComponents() {
        picker = new FilePicker(this);
        taskCoordinator = new TaskCoordinator();
        logExporter = new LogExporter(this, picker, this::runInBackground);
        artworkPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                updateRecycler();
            }
        });
        database = MiViajeDatabase.getInstance(getApplicationContext());
        cardRepository = new CardRepository(database);
        artworkRepository = new ArtworkRepository(this, database);
        backupService = new BackupService(database, artworkRepository);
        cardAdapter = new CardAdapter(this::onCardMenuAction);
        lastItemBottomSpacingDecoration = new LastItemBottomSpacingDecoration();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    private void setupCardRecycler() {
        cardRecycler = cardListUiStateController.getCardRecycler();
        cardRecycler.setLayoutManager(new LinearLayoutManager(this));
        cardRecycler.setAdapter(cardAdapter);
        cardRecycler.addItemDecoration(lastItemBottomSpacingDecoration);
    }

    private void setupAddCardButton() {
        addCardButton = findViewById(R.id.main_fab_card_add);
        configureMainInsets();
        addCardButton.setOnClickListener(v -> startCardScan());
    }

    private void updateRecycler() {
        cardListUiStateController.showLoading();

        runInBackground(() -> {
            try {
                CardListSnapshot snapshot = loadCardListSnapshot();
                runOnUiThread(() -> bindCardListSnapshot(snapshot));
            } catch (Exception e) {
                Log.w(TAG_CARD_LIST, "Error loading cards", e);
                runOnUiThread(() -> {
                    cardListUiStateController.showEmptyTip();
                    Toast.makeText(this, R.string.toast_background_sanitize_error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private CardListSnapshot loadCardListSnapshot() {
        ArtworkRepository.ArtworkSanitizationResult sanitizationResult = ensureArtworkSanitizedOnce();
        List<Card> cards = new ArrayList<>(cardRepository.getAllCards(1));
        Collections.reverse(cards);

        return new CardListSnapshot(cards, loadArtworkMap(), sanitizationResult);
    }

    private Map<String, Artwork> loadArtworkMap() {
        return artworkRepository.getMapById();
    }

    private ArtworkRepository.ArtworkSanitizationResult ensureArtworkSanitizedOnce() {
        synchronized (artworkSanitizationLock) {
            if (artworkSanitizationFinished) {
                return new ArtworkRepository.ArtworkSanitizationResult(0, 0, false);
            }
            ArtworkRepository.ArtworkSanitizationResult result = artworkRepository.sanitizeImportedArtworks();
            artworkSanitizationFinished = true;
            return result;
        }
    }

    private void showArtworkSanitizationFeedback(ArtworkRepository.ArtworkSanitizationResult result) {
        if (result == null) {
            return;
        }
        if (result.hadErrors) {
            Toast.makeText(this, R.string.toast_background_sanitize_error, Toast.LENGTH_LONG).show();
            return;
        }
        if (result.hasVisibleChanges()) {
            Toast.makeText(this, getString(R.string.toast_background_sanitized, result.optimizedCount, result.removedCount), Toast.LENGTH_LONG).show();
        }
    }

    private void bindCardListSnapshot(CardListSnapshot snapshot) {
        cardAdapter.updateArtworks(snapshot.artworksById);
        cardAdapter.updateCards(snapshot.cards);
        showArtworkSanitizationFeedback(snapshot.sanitizationResult);
        if (snapshot.cards.isEmpty()) {
            cardListUiStateController.showEmptyTip();
        } else {
            cardListUiStateController.showCards();
        }
    }

    private void showDeleteDialog(Card card) {
        new MaterialAlertDialogBuilder(this).setMessage(R.string.delete_card_confirm).setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.yes, (_dialog, _which) -> deleteCard(card)).show();
    }

    private void deleteCard(Card card) {
        runInBackground(() -> {
            cardRepository.delete(card.getUid());
            runOnUiThread(this::updateRecycler);
        });
    }

    private void showRenameDialog(Card card) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_card_rename, null);
        TextInputLayout aliasInputLayout = dialogView.findViewById(R.id.dialog_card_alias_input_layout);
        TextInputEditText aliasInput = dialogView.findViewById(R.id.dialog_card_alias_input);

        if (card.getAlias() != null) {
            aliasInput.setText(card.getAlias());
            aliasInput.setSelection(card.getAlias().length());
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            applyAliasDialogFallbackForPreS(aliasInputLayout, aliasInput);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this).setTitle(R.string.rename_card_title).setView(dialogView).setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.save, null).create();

        dialog.setOnShowListener(_dialog -> {
            aliasInput.requestFocus();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(_view -> {
                String alias = aliasInput.getText() == null ? null : aliasInput.getText().toString();
                updateCardAlias(card, normalizeAlias(alias));
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void updateCardAlias(Card card, String alias) {
        runInBackground(() -> {
            try {
                cardRepository.updateAlias(card.getUid(), alias);
                runOnUiThread(() -> {
                    updateRecycler();
                    int messageRes = alias == null ? R.string.toast_alias_cleared : R.string.toast_alias_updated;
                    Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.w(TAG_CARD_ALIAS, "Error while updating alias", e);
                runOnUiThread(() -> Toast.makeText(this, R.string.toast_alias_update_error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void applyAliasDialogFallbackForPreS(TextInputLayout inputLayout, TextInputEditText inputField) {
        int primaryColor = MaterialColors.getColor(inputLayout, androidx.appcompat.R.attr.colorPrimary, 0);
        int surfaceColor = MaterialColors.getColor(inputLayout, com.google.android.material.R.attr.colorSurface, 0);
        int onSurfaceColor = MaterialColors.getColor(inputLayout, com.google.android.material.R.attr.colorOnSurface, 0);

        ColorStateList strokeColors = new ColorStateList(new int[][]{new int[]{android.R.attr.state_focused}, new int[]{}}, new int[]{primaryColor, ColorUtils.setAlphaComponent(onSurfaceColor, 120)});
        ColorStateList hintColors = new ColorStateList(new int[][]{new int[]{android.R.attr.state_focused}, new int[]{}}, new int[]{primaryColor, ColorUtils.setAlphaComponent(onSurfaceColor, 180)});

        inputLayout.setBoxStrokeColorStateList(strokeColors);
        inputLayout.setHintTextColor(hintColors);
        inputLayout.setBoxBackgroundColor(surfaceColor);
        inputField.setTextColor(onSurfaceColor);
        inputField.setHintTextColor(ColorUtils.setAlphaComponent(onSurfaceColor, 180));
        inputField.setHighlightColor(ColorUtils.setAlphaComponent(primaryColor, 96));
    }

    private boolean onCardMenuAction(Card card, MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.card_action_change_background) {
            openArtworkPicker(card);
            return true;
        }
        if (itemId == R.id.card_action_rename) {
            showRenameDialog(card);
            return true;
        }
        if (itemId == R.id.card_action_copy_uid) {
            copyCardUidToClipboard(card);
            return true;
        }
        if (itemId == R.id.card_action_export) {
            showPendingCardExportMessage(card);
            return true;
        }
        if (itemId == R.id.card_action_delete) {
            showDeleteDialog(card);
            return true;
        }
        return false;
    }

    private void openArtworkPicker(Card card) {
        Intent intent = new Intent(this, ArtworkPickerActivity.class);
        intent.putExtra(ArtworkPickerActivity.EXTRA_CARD_UID, card.getUid());
        artworkPickerLauncher.launch(intent);
    }

    private void copyCardUidToClipboard(Card card) {
        String uid = card.getUidString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(getString(R.string.copy_uid), uid);
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(this, getString(R.string.toast_uid_copied, uid), Toast.LENGTH_SHORT).show();
    }

    private void showPendingCardExportMessage(Card card) {
        runInBackground(() -> {
            try {
                String json = backupService.exportCard(card.getUid());
                String fileName = backupService.buildCardExportFileName(card.getAlias(), card.getUid());
                runOnUiThread(() -> picker.saveJson(json, fileName));
            } catch (Exception e) {
                Log.w(TAG_CARD_EXPORT, "Error exporting card backup", e);
                runOnUiThread(() -> Toast.makeText(this, "Error al exportar tarjeta", Toast.LENGTH_LONG).show());
            }
        });
    }

    private void configureMainInsets() {
        int navAndCutoutTypes = WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.displayCutout();
        applyInsetsToMargins(addCardButton, navAndCutoutTypes, true, false, true, true);

        addCardButton.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> updateLastItemBottomSpacing());
        addCardButton.post(this::updateLastItemBottomSpacing);
    }

    private void updateLastItemBottomSpacing() {
        if (cardRecycler == null || addCardButton == null) {
            return;
        }

        int bottomMargin = 0;
        ViewGroup.LayoutParams layoutParams = addCardButton.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            bottomMargin = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }

        int spacing = addCardButton.getHeight() + bottomMargin + dpToPx(FAB_LAST_ITEM_EXTRA_SPACING_DP);
        if (lastItemBottomSpacingDecoration.setBottomSpacingPx(spacing)) {
            cardRecycler.invalidateItemDecorations();
        }
    }

    private void startCardScan() {
        if (nfcAdapter == null) {
            Toast.makeText(this, getString(R.string.toast_nfc_not_supported), Toast.LENGTH_SHORT).show();
        } else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, getString(R.string.toast_nfc_disabled), Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        } else {
            showScanBottomSheet();
        }
    }

    private void showScanBottomSheet() {
        scanSheetController = new CardScanSheetController(this, this);
        scanSheetController.show();
        showScanWaitingState();
    }

    private void showScanWaitingState() {
        synchronized (cardScanLock) {
            cardScanActive = true;
            cardScanInProgress = false;
            scanSheetState = ScanSheetState.WAITING;
        }

        scannedCard = null;
        scannedCardWasNew = false;
        scanSheetController.showWaiting();
        enableReaderModeIfAvailable();
    }

    private void showScanReadingState() {
        synchronized (cardScanLock) {
            if (!cardScanActive) {
                return;
            }
            scanSheetState = ScanSheetState.READING;
        }

        scanSheetController.showReading();
    }

    private void showScanSuccessState(SavedScannedCardResult result) {
        synchronized (cardScanLock) {
            cardScanInProgress = false;
            if (!cardScanActive) {
                return;
            }
            scanSheetState = ScanSheetState.SUCCESS;
        }

        scannedCard = result.card;
        scannedCardWasNew = result.isNewCard;
        scanSheetController.showSuccess(scannedCard, scannedCardWasNew);
    }

    private void showScanErrorState(Throwable error) {
        synchronized (cardScanLock) {
            cardScanInProgress = false;
            if (!cardScanActive) {
                return;
            }
            scanSheetState = ScanSheetState.ERROR;
        }

        scannedCard = null;
        scannedCardWasNew = false;
        scanSheetController.showError(error);
    }

    @Override
    public void onSaveAlias(String rawAlias) {
        Card card = scannedCard;
        if (card == null) {
            return;
        }

        String alias = normalizeAlias(rawAlias);
        scanSheetController.showAliasSaving();

        runInBackground(() -> {
            try {
                cardRepository.updateAlias(card.getUid(), alias);
                Card updatedCard = cardRepository.getCard(card.getUid(), 3);
                runOnUiThread(() -> onScannedCardAliasSaved(updatedCard == null ? card : updatedCard, alias));
            } catch (Exception e) {
                Log.w(TAG_CARD_ALIAS, "Error while updating scanned card alias", e);
                runOnUiThread(this::onScannedCardAliasSaveFailed);
            }
        });
    }

    private void onScannedCardAliasSaved(Card card, String alias) {
        updateRecycler();
        if (!isScanSheetInState(ScanSheetState.SUCCESS)) {
            return;
        }

        scannedCard = card;
        scanSheetController.showAliasSaved(card, alias);
    }

    private void onScannedCardAliasSaveFailed() {
        if (!isScanSheetInState(ScanSheetState.SUCCESS)) {
            return;
        }

        scanSheetController.showAliasSaveFailed();
    }

    private boolean isScanSheetInState(ScanSheetState state) {
        synchronized (cardScanLock) {
            return cardScanActive && scanSheetState == state;
        }
    }

    @Override
    public void onViewCard() {
        Card card = scannedCard;
        if (card == null) {
            return;
        }

        Intent intent = new Intent(this, CardActivity.class);
        intent.putExtra("CARD_UID", card.getUid());
        intent.putExtra("CARD_TN", buildCardTransitionName(card));
        dismissScanBottomSheet();
        startActivity(intent);
    }

    @Override
    public void onScanAgain() {
        showScanWaitingState();
    }

    private String buildCardTransitionName(Card card) {
        return CARD_TRANSITION_NAME_PREFIX + card.getUidString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldEnableReaderMode()) {
            enableReaderModeIfAvailable();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableReaderModeIfAvailable();
    }

    private void enableReaderModeIfAvailable() {
        if (nfcAdapter != null) {
            nfcAdapter.enableReaderMode(this, this, NFC_READER_FLAGS, null);
        }
    }

    private void disableReaderModeIfAvailable() {
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }

    private boolean shouldEnableReaderMode() {
        synchronized (cardScanLock) {
            return cardScanActive && !cardScanInProgress;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            openActivity(SettingsActivity.class);
            return true;
        }
        if (itemId == R.id.action_help) {
            openActivity(HelpActivity.class);
            return true;
        }
        if (itemId == R.id.action_about) {
            openActivity(AboutActivity.class);
            return true;
        }
        if (itemId == R.id.action_export) {
            exportAllData();
            return true;
        }
        if (itemId == R.id.action_import) {
            importAllData();
            return true;
        }
        if (itemId == R.id.action_export_log) {
            exportAppLog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openActivity(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    private void exportAllData() {
        runInBackground(() -> {
            String json = backupService.exportData();
            runOnUiThread(() -> exportJson(json));
        });
    }

    private void importAllData() {
        picker.loadJson(json -> runInBackground(() -> {
            try {
                BackupImportResult result = backupService.importData(json);
                if (result.isSuccess()) {
                    runOnUiThread(this::updateRecycler);
                    return;
                }

                String message = result.getErrors().isEmpty() ? "Error al importar respaldo" : result.getErrors().get(0).fieldPath + ": " + result.getErrors().get(0).message;
                runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                Log.e(TAG_IMPORT_DATA, "Error importing data", e);
                runOnUiThread(() -> Toast.makeText(this, "Error al importar respaldo", Toast.LENGTH_LONG).show());
            }
        }), err -> {
            Log.w(TAG_DATA_IMPORT, "Import failed", err);
            Toast.makeText(this, "Error al leer JSON", Toast.LENGTH_LONG).show();
        });
    }

    private void exportJson(String json) {
        picker.saveJson(json, String.format(Locale.getDefault(), "mi_viaje_backup_%s.json", LocalDate.now()));
    }

    private void exportAppLog() {
        logExporter.export(new LogExporter.Callback() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, R.string.toast_log_export_success, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCaptureError(Exception error) {
                Log.w(TAG_LOG_EXPORT, "Error capturing logcat", error);
                Toast.makeText(MainActivity.this, R.string.toast_log_export_capture_error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSaveError(Exception error) {
                Log.w(TAG_LOG_EXPORT, "Error saving log file", error);
                Toast.makeText(MainActivity.this, R.string.toast_log_export_save_error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onClearError(Exception error) {
                Log.w(TAG_LOG_EXPORT, "Log exported but buffer clear failed", error);
                Toast.makeText(MainActivity.this, R.string.toast_log_export_clear_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected int getOverflowMenuResId() {
        return R.menu.menu_main;
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        if (!tryStartCardRead())
            return;

        runOnUiThread(this::showScanReadingState);

        try {
            ScannedCardPayload payload = readScannedCard(tag);
            SavedScannedCardResult result = saveScannedCard(payload);

            runOnUiThread(() -> {
                updateRecycler();
                showScanSuccessState(result);
            });
        } catch (Exception e) {
            Log.w(TAG_CARD_SCAN, "Error while scanning/adding card", e);
            runOnUiThread(() -> showScanErrorState(e));
        }
    }

    private boolean tryStartCardRead() {
        synchronized (cardScanLock) {
            if (!cardScanActive || cardScanInProgress || scanSheetState != ScanSheetState.WAITING) {
                return false;
            }
            cardScanInProgress = true;
            return true;
        }
    }

    private ScannedCardPayload readScannedCard(Tag tag) throws Exception {
        try (IsoDep isoDep = IsoDep.get(tag)) {
            if (isoDep == null) {
                throw new InvalidObjectException("Tag NFC no soportado");
            }

            MiMovilidadParser cardParser = new MiMovilidadParser(isoDep);
            Card card = cardParser.readCard();
            return new ScannedCardPayload(card);
        }
    }

    private SavedScannedCardResult saveScannedCard(ScannedCardPayload payload) {
        boolean isNewCard = cardRepository.save(payload.card);
        Card savedCard = cardRepository.getCard(payload.card.getUid(), 3);
        return new SavedScannedCardResult(savedCard == null ? payload.card : savedCard, isNewCard);
    }

    private void dismissScanBottomSheet() {
        if (scanSheetController == null) {
            endCardScanFlow();
            return;
        }
        scanSheetController.dismiss();
    }

    @Override
    public void onDismissed() {
        endCardScanFlow();
        scanSheetController = null;
    }

    private void endCardScanFlow() {
        synchronized (cardScanLock) {
            cardScanActive = false;
            cardScanInProgress = false;
            scanSheetState = ScanSheetState.WAITING;
        }

        scannedCard = null;
        scannedCardWasNew = false;
        disableReaderModeIfAvailable();
    }

    private void runInBackground(Runnable task) {
        taskCoordinator.execute(task);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (taskCoordinator != null) {
            taskCoordinator.shutdown();
        }
    }

    private enum ScanSheetState {
        WAITING, READING, SUCCESS, ERROR
    }

    private static final class CardListSnapshot {
        final List<Card> cards;
        final Map<String, Artwork> artworksById;
        final ArtworkRepository.ArtworkSanitizationResult sanitizationResult;

        CardListSnapshot(List<Card> cards, Map<String, Artwork> artworksById, ArtworkRepository.ArtworkSanitizationResult sanitizationResult) {
            this.cards = cards;
            this.artworksById = artworksById;
            this.sanitizationResult = sanitizationResult;
        }
    }

    private static final class ScannedCardPayload {
        final Card card;

        ScannedCardPayload(Card card) {
            this.card = card;
        }
    }

    private static final class SavedScannedCardResult {
        final Card card;
        final boolean isNewCard;

        SavedScannedCardResult(Card card, boolean isNewCard) {
            this.card = card;
            this.isNewCard = isNewCard;
        }
    }

    private static final class TaskCoordinator {
        private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

        void execute(Runnable task) {
            ioExecutor.execute(task);
        }

        void shutdown() {
            ioExecutor.shutdownNow();
        }
    }

    private static final class LastItemBottomSpacingDecoration extends RecyclerView.ItemDecoration {
        private int bottomSpacingPx;

        public boolean setBottomSpacingPx(int bottomSpacingPx) {
            int safeBottomSpacing = Math.max(bottomSpacingPx, 0);
            if (this.bottomSpacingPx == safeBottomSpacing) {
                return false;
            }
            this.bottomSpacingPx = safeBottomSpacing;
            return true;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            RecyclerView.Adapter<?> adapter = parent.getAdapter();
            int position = parent.getChildAdapterPosition(view);
            if (adapter == null || position == RecyclerView.NO_POSITION) {
                return;
            }

            outRect.bottom = position == adapter.getItemCount() - 1 ? bottomSpacingPx : 0;
        }
    }
}
