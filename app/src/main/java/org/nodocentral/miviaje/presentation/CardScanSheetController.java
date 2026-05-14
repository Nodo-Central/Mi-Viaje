package org.nodocentral.miviaje.presentation;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.domain.mimovilidad.card.Card;

final class CardScanSheetController {
    private final Activity activity;
    private final Listener listener;
    private BottomSheetDialog dialog;
    private View statusFrame;
    private ImageView heroIcon;
    private ImageView successIcon;
    private CircularProgressIndicator progress;
    private TextView title;
    private TextView message;
    private TextView detail;
    private TextInputLayout aliasInputLayout;
    private TextInputEditText aliasInput;
    private MaterialButton viewCardButton;
    private MaterialButton scanAgainButton;
    private MaterialButton nameCardButton;
    private MaterialButton doneButton;
    private View secondaryActions;
    private Card card;
    CardScanSheetController(Activity activity, Listener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    void show() {
        View dialogView = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_card_scan, null);
        bind(dialogView);

        dialog = new BottomSheetDialog(activity);
        dialog.setContentView(dialogView);
        dialog.setOnDismissListener(_dialog -> listener.onDismissed());
        dialog.show();
    }

    void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    void showWaiting() {
        card = null;
        setText(R.string.card_scan_title_waiting, R.string.card_scan_message_waiting, activity.getString(R.string.card_scan_detail_waiting));
        showIconState(true, false, false);
        applyStatusTone(androidx.appcompat.R.attr.colorPrimary);
        setSuccessActionsVisible(false);
        setAliasEditorVisible(false);
        scanAgainButton.setText(R.string.card_scan_action_scan_again);
        nameCardButton.setVisibility(View.VISIBLE);
        nameCardButton.setText(R.string.card_scan_action_name_card);
        nameCardButton.setEnabled(true);
        doneButton.setText(R.string.cancel);
        doneButton.setTextColor(getAttrColor(androidx.appcompat.R.attr.colorPrimary));
    }

    void showReading() {
        setText(R.string.card_scan_title_reading, R.string.card_scan_message_reading, activity.getString(R.string.card_scan_detail_reading));
        showIconState(false, true, false);
        applyStatusTone(com.google.android.material.R.attr.colorSecondary);
        setSuccessActionsVisible(false);
        setAliasEditorVisible(false);
        doneButton.setText(R.string.cancel);
        doneButton.setVisibility(TextView.GONE);
    }

    void showSuccess(Card card, boolean isNewCard) {
        this.card = card;

        int messageRes = isNewCard ? R.string.card_scan_message_success_added : R.string.card_scan_message_success_updated;
        setText(R.string.card_scan_title_success, messageRes, activity.getString(R.string.uid_format, card.getUidString()));
        showIconState(false, false, true);
        applyStatusTone(com.google.android.material.R.attr.colorTertiary);
        setAliasEditorVisible(false);
        setSuccessActionsVisible(true);
        scanAgainButton.setText(R.string.card_scan_action_scan_again);
        nameCardButton.setVisibility(View.VISIBLE);
        nameCardButton.setText(R.string.card_scan_action_name_card);
        nameCardButton.setEnabled(true);
        doneButton.setText(R.string.card_scan_action_done);
        doneButton.setTextColor(getAttrColor(com.google.android.material.R.attr.colorTertiary));
        doneButton.setVisibility(TextView.VISIBLE);

        setAliasInputText(card.getAlias());
    }

    void showError(Throwable error) {
        card = null;

        String errorMessage = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        setText(R.string.card_scan_title_error, R.string.card_scan_message_error, activity.getString(R.string.card_scan_detail_error, errorMessage));
        showIconState(true, false, false);
        applyStatusTone(com.google.android.material.R.attr.colorErrorContainer);
        setAliasEditorVisible(false);
        setSuccessActionsVisible(true);
        viewCardButton.setVisibility(View.GONE);
        scanAgainButton.setText(R.string.card_scan_action_try_again);
        nameCardButton.setVisibility(View.GONE);
        doneButton.setText(R.string.card_scan_action_done);
    }

    void showAliasSaving() {
        nameCardButton.setEnabled(false);
        aliasInput.setEnabled(false);
        aliasInputLayout.setHelperText(activity.getString(R.string.card_scan_alias_saving));
    }

    void showAliasSaved(Card card, String alias) {
        this.card = card;
        nameCardButton.setEnabled(true);
        aliasInput.setEnabled(true);
        nameCardButton.setText(R.string.card_scan_action_name_card);
        aliasInputLayout.setVisibility(View.GONE);
        detail.setText(alias == null ? R.string.card_scan_alias_cleared : R.string.card_scan_alias_saved);
    }

    void showAliasSaveFailed() {
        nameCardButton.setEnabled(true);
        aliasInput.setEnabled(true);
        aliasInputLayout.setHelperText(activity.getString(R.string.card_scan_alias_error));
    }

    private void bind(View dialogView) {
        statusFrame = dialogView.findViewById(R.id.frameLayout);
        heroIcon = dialogView.findViewById(R.id.card_scan_hero_icon);
        successIcon = dialogView.findViewById(R.id.card_scan_success_icon);
        progress = dialogView.findViewById(R.id.card_scan_progress);
        title = dialogView.findViewById(R.id.card_scan_title);
        message = dialogView.findViewById(R.id.card_scan_message);
        detail = dialogView.findViewById(R.id.card_scan_detail);
        aliasInputLayout = dialogView.findViewById(R.id.card_scan_alias_input_layout);
        aliasInput = dialogView.findViewById(R.id.card_scan_alias_input);
        viewCardButton = dialogView.findViewById(R.id.card_scan_button_view_card);
        scanAgainButton = dialogView.findViewById(R.id.card_scan_button_scan_again);
        nameCardButton = dialogView.findViewById(R.id.card_scan_button_name_card);
        doneButton = dialogView.findViewById(R.id.bottom_sheet_button_close);
        secondaryActions = dialogView.findViewById(R.id.card_scan_secondary_actions);

        viewCardButton.setOnClickListener(_view -> listener.onViewCard());
        scanAgainButton.setOnClickListener(_view -> listener.onScanAgain());
        nameCardButton.setOnClickListener(_view -> onNameCardClicked());
        doneButton.setOnClickListener(_view -> dismiss());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            applyAliasInputFallbackForPreS();
        }
    }

    private void onNameCardClicked() {
        if (card == null) {
            return;
        }

        if (aliasInputLayout.getVisibility() != View.VISIBLE) {
            showAliasEditor();
            return;
        }

        String alias = aliasInput.getText() == null ? null : aliasInput.getText().toString();
        listener.onSaveAlias(alias);
    }

    private void showAliasEditor() {
        setAliasInputText(card.getAlias());
        aliasInputLayout.setVisibility(View.VISIBLE);
        aliasInputLayout.setHelperText(activity.getString(R.string.card_scan_alias_helper));
        nameCardButton.setText(R.string.card_scan_action_save_name);
        aliasInput.requestFocus();
    }

    private void setAliasInputText(String alias) {
        aliasInput.setText(alias);
        if (alias != null) {
            aliasInput.setSelection(alias.length());
        }
    }

    private void setText(int titleRes, int messageRes, String detailText) {
        title.setText(titleRes);
        message.setText(messageRes);
        detail.setText(detailText);
    }

    private void showIconState(boolean showHeroIcon, boolean showProgress, boolean showSuccessIcon) {
        heroIcon.setVisibility(showHeroIcon ? View.VISIBLE : View.GONE);
        progress.setVisibility(showProgress ? View.VISIBLE : View.GONE);
        successIcon.setVisibility(showSuccessIcon ? View.VISIBLE : View.GONE);
    }

    private void setSuccessActionsVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        viewCardButton.setVisibility(visibility);
        secondaryActions.setVisibility(visibility);
    }

    private void setAliasEditorVisible(boolean visible) {
        aliasInputLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        aliasInput.setEnabled(true);
        aliasInputLayout.setHelperText(activity.getString(R.string.card_scan_alias_helper));
    }

    private void applyStatusTone(int statusColorRes) {
        statusFrame.setBackgroundTintList(ColorStateList.valueOf(MaterialColors.getColor(activity, statusColorRes, 0)));
    }

    private ColorStateList getAttrColor(int attr) {
        return ColorStateList.valueOf(MaterialColors.getColor(activity, attr, 0));
    }

    private void applyAliasInputFallbackForPreS() {
        int primaryColor = MaterialColors.getColor(aliasInputLayout, androidx.appcompat.R.attr.colorPrimary, 0);
        int surfaceColor = MaterialColors.getColor(aliasInputLayout, com.google.android.material.R.attr.colorSurface, 0);
        int onSurfaceColor = MaterialColors.getColor(aliasInputLayout, com.google.android.material.R.attr.colorOnSurface, 0);

        ColorStateList strokeColors = new ColorStateList(new int[][]{new int[]{android.R.attr.state_focused}, new int[]{}}, new int[]{primaryColor, ColorUtils.setAlphaComponent(onSurfaceColor, 120)});
        ColorStateList hintColors = new ColorStateList(new int[][]{new int[]{android.R.attr.state_focused}, new int[]{}}, new int[]{primaryColor, ColorUtils.setAlphaComponent(onSurfaceColor, 180)});

        aliasInputLayout.setBoxStrokeColorStateList(strokeColors);
        aliasInputLayout.setHintTextColor(hintColors);
        aliasInputLayout.setBoxBackgroundColor(surfaceColor);
        aliasInput.setTextColor(onSurfaceColor);
        aliasInput.setHintTextColor(ColorUtils.setAlphaComponent(onSurfaceColor, 180));
        aliasInput.setHighlightColor(ColorUtils.setAlphaComponent(primaryColor, 96));
    }

    interface Listener {
        void onDismissed();

        void onViewCard();

        void onScanAgain();

        void onSaveAlias(String alias);
    }
}
