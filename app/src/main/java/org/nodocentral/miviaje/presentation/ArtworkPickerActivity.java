package org.nodocentral.miviaje.presentation;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.yalantis.ucrop.UCrop;

import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.data.artwork.CardArtworkResolver;
import org.nodocentral.miviaje.data.files.ArtworkImageManager;
import org.nodocentral.miviaje.data.repository.ArtworkRepository;
import org.nodocentral.miviaje.data.repository.CardRepository;
import org.nodocentral.miviaje.data.room.MiViajeDatabase;
import org.nodocentral.miviaje.domain.artwork.Artwork;
import org.nodocentral.miviaje.domain.mimovilidad.card.Card;
import org.nodocentral.miviaje.presentation.adapters.ArtworkOptionAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArtworkPickerActivity extends BaseActivity implements ArtworkOptionAdapter.Listener {
    public static final String EXTRA_CARD_UID = "org.nodocentral.miviaje.extra.CARD_UID";

    private static final String TAG_ARTWORK = "ARTWORK_PICKER";
    private static final String STATE_IMPORT_AS_IS = "import_as_is";
    private static final String STATE_PENDING_CROP_OUTPUT_URI = "pending_crop_output_uri";
    private static final String STATE_PENDING_IMPORT_DISPLAY_NAME = "pending_import_display_name";
    private static final int CARD_ARTWORK_ASPECT_WIDTH = 16;
    private static final int CARD_ARTWORK_ASPECT_HEIGHT = 10;
    private static final int MIN_GRID_ITEM_WIDTH_DP = 320;

    private MiViajeDatabase database;
    private CardRepository cardRepository;
    private ArtworkRepository artworkRepository;
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;
    private FloatingActionButton importButton;
    private MaterialSwitch importAsIsSwitch;
    private ArtworkOptionAdapter adapter;
    private ActivityResultLauncher<String[]> openImageLauncher;
    private ActivityResultLauncher<Intent> cropImageLauncher;
    private ExecutorService ioExecutor;
    private long cardUid;
    private Card card;
    private boolean importAsIs;
    private Uri pendingCropOutputUri;
    private String pendingImportDisplayName;

    private static String normalizeArtworkDisplayName(String displayName) {
        return normalizeOptionalText(displayName);
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
        setContentView(R.layout.activity_artwork_picker);
        setToolbar(true);

        cardUid = getIntent().getLongExtra(EXTRA_CARD_UID, -1L);
        if (cardUid <= 0L) {
            finish();
            return;
        }

        restoreImportState(savedInstanceState);
        cropImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onCropResult);
        openImageLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onBackgroundImagePicked);
        initializeCoreComponents();
        setupRecycler();
        setupImportButton();
        configureInsets();
        loadCardAndOptions();
    }

    private void initializeCoreComponents() {
        database = MiViajeDatabase.getInstance(getApplicationContext());
        cardRepository = new CardRepository(database);
        artworkRepository = new ArtworkRepository(this, database);
        ioExecutor = Executors.newSingleThreadExecutor();
        adapter = new ArtworkOptionAdapter(this);
    }

    private void setupRecycler() {
        recyclerView = findViewById(R.id.artwork_picker_recycler);
        gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> updateGridSpan());
        recyclerView.post(this::updateGridSpan);
    }

    private void setupImportButton() {
        importButton = findViewById(R.id.artwork_picker_import_fab);
        importAsIsSwitch = findViewById(R.id.artwork_picker_import_as_is_switch);
        importAsIsSwitch.setChecked(importAsIs);
        importAsIsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> importAsIs = isChecked);
        importButton.setOnClickListener(v -> openImageLauncher.launch(new String[]{"image/*"}));
    }

    private void configureInsets() {
        int navAndCutoutTypes = WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.displayCutout();
        applyInsetsToPadding(recyclerView, navAndCutoutTypes, true, false, true, true);
        applyInsetsToMargins(importButton, navAndCutoutTypes, true, false, true, true);
        applyInsetsToMargins(importAsIsSwitch, navAndCutoutTypes, true, false, false, true);
    }

    private void restoreImportState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        importAsIs = savedInstanceState.getBoolean(STATE_IMPORT_AS_IS, false);
        pendingImportDisplayName = savedInstanceState.getString(STATE_PENDING_IMPORT_DISPLAY_NAME);
        String pendingCropOutput = savedInstanceState.getString(STATE_PENDING_CROP_OUTPUT_URI);
        if (pendingCropOutput != null) {
            pendingCropOutputUri = Uri.parse(pendingCropOutput);
        }
    }

    private void updateGridSpan() {
        if (recyclerView == null || gridLayoutManager == null) {
            return;
        }

        int availableWidth = recyclerView.getWidth() - recyclerView.getPaddingLeft() - recyclerView.getPaddingRight();
        int spanCount = Math.max(1, availableWidth / dpToPx(MIN_GRID_ITEM_WIDTH_DP));
        if (gridLayoutManager.getSpanCount() != spanCount) {
            gridLayoutManager.setSpanCount(spanCount);
        }
    }

    private void loadCardAndOptions() {
        runInBackground(() -> {
            Card loadedCard = cardRepository.getCard(cardUid, 3);
            if (loadedCard == null) {
                runOnUiThread(this::finish);
                return;
            }

            List<ArtworkOptionAdapter.ArtworkOption> options = buildArtworkOptions(loadedCard);
            runOnUiThread(() -> {
                card = loadedCard;
                adapter.submitList(options);
            });
        });
    }

    private List<ArtworkOptionAdapter.ArtworkOption> buildArtworkOptions(Card loadedCard) {
        String currentArtworkRef = normalizeOptionalText(loadedCard.getArtworkRef());
        List<ArtworkOptionAdapter.ArtworkOption> explicitOptions = new ArrayList<>();
        boolean hasExplicitMatch = false;

        for (CardArtworkResolver.BuiltinOption builtinOption : CardArtworkResolver.getBuiltinOptions()) {
            String artworkRef = CardArtworkResolver.toBuiltinRef(builtinOption.key);
            boolean selected = artworkRef.equals(currentArtworkRef);
            hasExplicitMatch |= selected;
            explicitOptions.add(new ArtworkOptionAdapter.ArtworkOption(
                    ArtworkOptionAdapter.OptionType.BUILTIN,
                    artworkRef,
                    getString(builtinOption.labelResId),
                    null,
                    builtinOption.drawableResId,
                    0,
                    selected
            ));
        }

        for (Artwork artwork : artworkRepository.getAll()) {
            String artworkRef = CardArtworkResolver.toArtworkRef(artwork.getId());
            boolean selected = artworkRef.equals(currentArtworkRef);
            hasExplicitMatch |= selected;
            explicitOptions.add(new ArtworkOptionAdapter.ArtworkOption(
                    ArtworkOptionAdapter.OptionType.IMPORTED,
                    artworkRef,
                    getString(R.string.artwork_option_imported_named, getArtworkDisplayName(artwork)),
                    artwork,
                    0,
                    artworkRepository.countCardUsage(artwork.getId()),
                    selected
            ));
        }

        List<ArtworkOptionAdapter.ArtworkOption> options = new ArrayList<>(explicitOptions.size() + 1);
        boolean defaultSelected = currentArtworkRef == null || !hasExplicitMatch;
        options.add(new ArtworkOptionAdapter.ArtworkOption(
                ArtworkOptionAdapter.OptionType.DEFAULT,
                null,
                getString(R.string.artwork_option_default),
                null,
                CardArtworkResolver.resolveAutomaticBackground(loadedCard),
                0,
                defaultSelected
        ));
        options.addAll(explicitOptions);
        return options;
    }

    @Override
    public void onArtworkSelected(ArtworkOptionAdapter.ArtworkOption option) {
        Card currentCard = card;
        if (currentCard == null) {
            return;
        }

        runInBackground(() -> {
            try {
                artworkRepository.assignToCard(currentCard.getUid(), option.artworkRef);
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.toast_background_updated, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                Log.w(TAG_ARTWORK, "Error updating artwork", e);
                runOnUiThread(() -> Toast.makeText(this, R.string.toast_background_update_error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onImportedArtworkMenuClicked(View anchor, ArtworkOptionAdapter.ArtworkOption option) {
        PopupMenu popupMenu = new PopupMenu(
                new ContextThemeWrapper(this, R.style.ThemeOverlay_MiViaje_PopupMenu),
                anchor
        );
        popupMenu.getMenuInflater().inflate(R.menu.popup_artwork, popupMenu.getMenu());
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(item -> onArtworkMenuAction(option, item));
        popupMenu.show();
    }

    private boolean onArtworkMenuAction(ArtworkOptionAdapter.ArtworkOption option, MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.artwork_action_rename) {
            showRenameArtworkDialog(option.artwork);
            return true;
        }
        if (itemId == R.id.artwork_action_delete) {
            confirmArtworkDeletion(option);
            return true;
        }
        return false;
    }

    private void showRenameArtworkDialog(Artwork artwork) {
        if (artwork == null) {
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_artwork_rename, null);
        TextInputLayout nameInputLayout = dialogView.findViewById(R.id.dialog_artwork_name_input_layout);
        TextInputEditText nameInput = dialogView.findViewById(R.id.dialog_artwork_name_input);

        String currentName = normalizeArtworkDisplayName(artwork.getDisplayName());
        if (currentName != null) {
            nameInput.setText(currentName);
            nameInput.setSelection(currentName.length());
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            applyArtworkDialogFallbackForPreS(nameInputLayout, nameInput);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.rename_imported_artwork_title)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();

        dialog.setOnShowListener(_dialog -> {
            nameInput.requestFocus();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(_view -> {
                String displayName = nameInput.getText() == null ? null : nameInput.getText().toString();
                updateArtworkDisplayName(artwork, displayName);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void applyArtworkDialogFallbackForPreS(TextInputLayout inputLayout, TextInputEditText inputField) {
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

    private void updateArtworkDisplayName(Artwork artwork, String displayName) {
        runInBackground(() -> {
            try {
                artworkRepository.updateDisplayName(artwork.getId(), displayName);
                runOnUiThread(() -> {
                    setResult(RESULT_OK);
                    loadCardAndOptions();
                    Toast.makeText(this, R.string.toast_artwork_renamed, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.w(TAG_ARTWORK, "Error renaming artwork", e);
                runOnUiThread(() -> Toast.makeText(this, R.string.toast_artwork_rename_error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void confirmArtworkDeletion(ArtworkOptionAdapter.ArtworkOption option) {
        if (option.usageCount > 0) {
            showDeleteInUseWarning(option);
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_imported_background_title)
                .setMessage(getString(R.string.delete_imported_background_confirm, getArtworkDisplayName(option.artwork)))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.yes, (_dialog, _which) -> deleteArtwork(option.artwork, false))
                .show();
    }

    private void showDeleteInUseWarning(ArtworkOptionAdapter.ArtworkOption option) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.artwork_delete_in_use_warning_title)
                .setMessage(getString(R.string.artwork_delete_in_use_warning_message, getArtworkDisplayName(option.artwork), option.usageCount))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.continue_action, (_dialog, _which) -> showDeleteInUseFinalConfirmation(option))
                .show();
    }

    private void showDeleteInUseFinalConfirmation(ArtworkOptionAdapter.ArtworkOption option) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.artwork_delete_in_use_final_title)
                .setMessage(getString(R.string.artwork_delete_in_use_final_message, getArtworkDisplayName(option.artwork)))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (_dialog, _which) -> deleteArtwork(option.artwork, true))
                .show();
    }

    private void deleteArtwork(Artwork artwork, boolean clearReferences) {
        runInBackground(() -> {
            try {
                if (clearReferences) {
                    artworkRepository.deleteAndClearReferences(artwork);
                } else {
                    artworkRepository.delete(artwork);
                }
                runOnUiThread(() -> {
                    setResult(RESULT_OK);
                    loadCardAndOptions();
                    Toast.makeText(this, R.string.toast_background_deleted, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.w(TAG_ARTWORK, "Error deleting artwork", e);
                runOnUiThread(() -> Toast.makeText(this, R.string.toast_background_delete_error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void onBackgroundImagePicked(Uri uri) {
        Card currentCard = card;
        if (uri == null || currentCard == null) {
            return;
        }
        if (importAsIs) {
            clearPendingCropState();
            importBackgroundAndAssign(uri, currentCard.getUid());
            return;
        }
        pendingImportDisplayName = artworkRepository.queryImportDisplayName(uri);
        startArtworkCrop(uri);
    }

    private void importBackgroundAndAssign(Uri sourceUri, long targetCardUid) {
        importBackgroundAndAssign(sourceUri, targetCardUid, null, false);
    }

    private void importBackgroundAndAssign(Uri sourceUri,
                                           long targetCardUid,
                                           String displayNameOverride,
                                           boolean deleteSourceAfterImport) {
        runInBackground(() -> {
            try {
                artworkRepository.importAndAssign(sourceUri, targetCardUid, displayNameOverride);
                runOnUiThread(() -> {
                    setResult(RESULT_OK);
                    Toast.makeText(this, R.string.toast_background_imported, Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (ArtworkRepository.ArtworkImportException e) {
                Log.w(TAG_ARTWORK, "Invalid image selected for import", e);
                runOnUiThread(() -> Toast.makeText(this, R.string.toast_background_import_invalid_image, Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                Log.w(TAG_ARTWORK, "Error importing artwork", e);
                runOnUiThread(() -> Toast.makeText(this, R.string.toast_background_import_error, Toast.LENGTH_SHORT).show());
            } finally {
                if (deleteSourceAfterImport) {
                    deleteFileUri(sourceUri);
                    runOnUiThread(this::clearPendingCropState);
                }
            }
        });
    }

    private void startArtworkCrop(Uri sourceUri) {
        try {
            Uri destinationUri = createCropOutputUri();
            pendingCropOutputUri = destinationUri;
            UCrop.Options options = createCropOptions();
            UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(CARD_ARTWORK_ASPECT_WIDTH, CARD_ARTWORK_ASPECT_HEIGHT)
                    .withMaxResultSize(
                            ArtworkImageManager.MAX_ARTWORK_SIDE_PX,
                            Math.round(ArtworkImageManager.MAX_ARTWORK_SIDE_PX
                                    * ((float) CARD_ARTWORK_ASPECT_HEIGHT / CARD_ARTWORK_ASPECT_WIDTH))
                    )
                    .withOptions(options)
                    .start(this, cropImageLauncher);
        } catch (Exception e) {
            Log.w(TAG_ARTWORK, "Error starting artwork crop", e);
            deletePendingCropOutput();
            Toast.makeText(this, R.string.toast_background_crop_error, Toast.LENGTH_SHORT).show();
        }
    }

    private UCrop.Options createCropOptions() {
        int primaryColor = MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary, 0);
        int surfaceColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, 0);
        int onSurfaceColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, 0);

        UCrop.Options options = new UCrop.Options();
        options.setToolbarTitle(getString(R.string.artwork_crop_title));
        options.setToolbarColor(surfaceColor);
        options.setToolbarWidgetColor(onSurfaceColor);
        options.setActiveControlsWidgetColor(primaryColor);
        options.setRootViewBackgroundColor(surfaceColor);
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(95);
        options.setShowCropGrid(true);
        options.setFreeStyleCropEnabled(false);
        return options;
    }

    private Uri createCropOutputUri() {
        File cropDir = new File(getCacheDir(), "artwork-crops");
        if (!cropDir.exists() && !cropDir.mkdirs()) {
            throw new IllegalStateException("Could not create artwork crop cache dir");
        }
        File outputFile = new File(cropDir, "artwork-crop-" + System.currentTimeMillis() + ".jpg");
        return Uri.fromFile(outputFile);
    }

    private void onCropResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();
            Uri resultUri = data == null ? null : UCrop.getOutput(data);
            if (resultUri == null) {
                resultUri = pendingCropOutputUri;
            }
            if (resultUri == null || cardUid <= 0L) {
                deletePendingCropOutput();
                Toast.makeText(this, R.string.toast_background_crop_error, Toast.LENGTH_SHORT).show();
                return;
            }
            importBackgroundAndAssign(resultUri, cardUid, pendingImportDisplayName, true);
            return;
        }

        if (result.getResultCode() == UCrop.RESULT_ERROR) {
            Intent data = result.getData();
            Throwable cropError = data == null ? null : UCrop.getError(data);
            Log.w(TAG_ARTWORK, "Error cropping artwork", cropError);
            Toast.makeText(this, R.string.toast_background_crop_error, Toast.LENGTH_SHORT).show();
        }
        deletePendingCropOutput();
    }

    private void deletePendingCropOutput() {
        deleteFileUri(pendingCropOutputUri);
        clearPendingCropState();
    }

    private void clearPendingCropState() {
        pendingCropOutputUri = null;
        pendingImportDisplayName = null;
    }

    private static void deleteFileUri(Uri uri) {
        if (uri == null || !"file".equals(uri.getScheme()) || uri.getPath() == null) {
            return;
        }
        File file = new File(uri.getPath());
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    private String getArtworkDisplayName(Artwork artwork) {
        String displayName = artwork == null ? null : normalizeArtworkDisplayName(artwork.getDisplayName());
        return displayName != null ? displayName : getString(R.string.artwork_default_name);
    }

    private void runInBackground(Runnable task) {
        ioExecutor.execute(task);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_IMPORT_AS_IS, importAsIs);
        outState.putString(STATE_PENDING_IMPORT_DISPLAY_NAME, pendingImportDisplayName);
        if (pendingCropOutputUri != null) {
            outState.putString(STATE_PENDING_CROP_OUTPUT_URI, pendingCropOutputUri.toString());
        }
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
        if (ioExecutor != null) {
            ioExecutor.shutdownNow();
        }
        super.onDestroy();
    }
}
