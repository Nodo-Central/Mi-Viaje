package org.nodocentral.miviaje.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import org.nodocentral.miviaje.R;

import java.util.ArrayList;
import java.util.List;

public final class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public enum ChoiceKey {
        COLOR_THEME,
        LANGUAGE
    }

    public enum SwitchKey {
        PURE_DARK,
        ADVANCED_DATA,
        TECHNICAL_DATA,
        REBEL_MODE
    }

    public interface Listener {
        void onThemeSelected(String nightMode);

        void onChoiceClicked(ChoiceKey key);

        void onSwitchChanged(SwitchKey key, boolean checked);
    }

    public abstract static class Item {
        static final int TYPE_HEADER = 0;
        static final int TYPE_THEME = 1;
        static final int TYPE_CHOICE = 2;
        static final int TYPE_SWITCH = 3;

        final int type;

        Item(int type) {
            this.type = type;
        }
    }

    public static final class HeaderItem extends Item {
        final int titleResId;
        final int iconResId;

        public HeaderItem(int titleResId) {
            this(titleResId, 0);
        }

        public HeaderItem(int titleResId, @DrawableRes int iconResId) {
            super(TYPE_HEADER);
            this.titleResId = titleResId;
            this.iconResId = iconResId;
        }
    }

    public static final class ThemeItem extends GroupedItem {
        String selectedNightMode;
        final int lightIconResId;
        final int darkIconResId;
        final int systemIconResId;

        public ThemeItem(String selectedNightMode, boolean firstInGroup, boolean lastInGroup) {
            this(selectedNightMode, 0, 0, 0, firstInGroup, lastInGroup);
        }

        public ThemeItem(String selectedNightMode,
                         @DrawableRes int lightIconResId,
                         @DrawableRes int darkIconResId,
                         @DrawableRes int systemIconResId,
                         boolean firstInGroup,
                         boolean lastInGroup) {
            super(TYPE_THEME, firstInGroup, lastInGroup);
            this.selectedNightMode = selectedNightMode;
            this.lightIconResId = lightIconResId;
            this.darkIconResId = darkIconResId;
            this.systemIconResId = systemIconResId;
        }
    }

    public static final class ChoiceItem extends GroupedItem {
        final ChoiceKey key;
        final int titleResId;
        final int summaryResId;
        final int iconResId;

        public ChoiceItem(ChoiceKey key,
                          int titleResId,
                          int summaryResId,
                          boolean firstInGroup,
                          boolean lastInGroup) {
            this(key, titleResId, summaryResId, 0, firstInGroup, lastInGroup);
        }

        public ChoiceItem(ChoiceKey key,
                          int titleResId,
                          int summaryResId,
                          @DrawableRes int iconResId,
                          boolean firstInGroup,
                          boolean lastInGroup) {
            super(TYPE_CHOICE, firstInGroup, lastInGroup);
            this.key = key;
            this.titleResId = titleResId;
            this.summaryResId = summaryResId;
            this.iconResId = iconResId;
        }
    }

    public static final class SwitchItem extends GroupedItem {
        final SwitchKey key;
        final int titleResId;
        final int summaryResId;
        final int iconResId;
        boolean checked;

        public SwitchItem(SwitchKey key,
                          int titleResId,
                          int summaryResId,
                          boolean checked,
                          boolean firstInGroup,
                          boolean lastInGroup) {
            this(key, titleResId, summaryResId, 0, checked, firstInGroup, lastInGroup);
        }

        public SwitchItem(SwitchKey key,
                          int titleResId,
                          int summaryResId,
                          @DrawableRes int iconResId,
                          boolean checked,
                          boolean firstInGroup,
                          boolean lastInGroup) {
            super(TYPE_SWITCH, firstInGroup, lastInGroup);
            this.key = key;
            this.titleResId = titleResId;
            this.summaryResId = summaryResId;
            this.iconResId = iconResId;
            this.checked = checked;
        }
    }

    private abstract static class GroupedItem extends Item {
        final boolean firstInGroup;
        final boolean lastInGroup;

        GroupedItem(int type, boolean firstInGroup, boolean lastInGroup) {
            super(type);
            this.firstInGroup = firstInGroup;
            this.lastInGroup = lastInGroup;
        }
    }

    private static final String NIGHT_MODE_LIGHT = "light";
    private static final String NIGHT_MODE_DARK = "dark";
    private static final String NIGHT_MODE_SYSTEM = "system";
    private static final int GROUP_CORNER_RADIUS_DP = 24;
    private static final int COMPACT_SWITCH_MIN_HEIGHT_DP = 64;
    private static final int REGULAR_SWITCH_MIN_HEIGHT_DP = 88;
    private static final int COMPACT_SWITCH_VERTICAL_PADDING_DP = 10;
    private static final int REGULAR_SWITCH_VERTICAL_PADDING_DP = 14;

    private final List<Item> items = new ArrayList<>();
    private final Listener listener;

    public SettingsAdapter(Listener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == Item.TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_settings_header, parent, false);
            return new HeaderViewHolder(view);
        }
        if (viewType == Item.TYPE_THEME) {
            View view = inflater.inflate(R.layout.item_settings_theme, parent, false);
            return new ThemeViewHolder(view);
        }
        if (viewType == Item.TYPE_CHOICE) {
            View view = inflater.inflate(R.layout.item_settings_choice, parent, false);
            return new ChoiceViewHolder(view);
        }
        View view = inflater.inflate(R.layout.item_settings_switch, parent, false);
        return new SwitchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((HeaderItem) item);
        } else if (holder instanceof ThemeViewHolder) {
            ((ThemeViewHolder) holder).bind((ThemeItem) item, listener);
        } else if (holder instanceof ChoiceViewHolder) {
            ((ChoiceViewHolder) holder).bind((ChoiceItem) item, listener);
        } else if (holder instanceof SwitchViewHolder) {
            ((SwitchViewHolder) holder).bind((SwitchItem) item, listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(@NonNull List<Item> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void updateThemeSelection(String nightMode) {
        for (Item item : items) {
            if (item instanceof ThemeItem) {
                ((ThemeItem) item).selectedNightMode = nightMode;
                return;
            }
        }
    }

    public void updateSwitchChecked(SwitchKey key, boolean checked) {
        for (Item item : items) {
            if (item instanceof SwitchItem) {
                SwitchItem switchItem = (SwitchItem) item;
                if (switchItem.key == key) {
                    switchItem.checked = checked;
                    return;
                }
            }
        }
    }

    private static void applyGroupedCardShape(MaterialCardView card, GroupedItem item) {
        float radius = dp(card, GROUP_CORNER_RADIUS_DP);
        ShapeAppearanceModel shape = ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, item.firstInGroup ? radius : 0)
                .setTopRightCorner(CornerFamily.ROUNDED, item.firstInGroup ? radius : 0)
                .setBottomLeftCorner(CornerFamily.ROUNDED, item.lastInGroup ? radius : 0)
                .setBottomRightCorner(CornerFamily.ROUNDED, item.lastInGroup ? radius : 0)
                .build();
        card.setShapeAppearanceModel(shape);
    }

    private static void setDividerVisibility(View divider, GroupedItem item) {
        divider.setVisibility(item.lastInGroup ? View.GONE : View.VISIBLE);
    }

    private static void bindIcon(ImageView icon, @DrawableRes int iconResId) {
        if (iconResId == 0) {
            icon.setImageDrawable(null);
            icon.setVisibility(View.GONE);
            return;
        }
        icon.setImageResource(iconResId);
        icon.setVisibility(View.VISIBLE);
    }

    private static void bindButtonIcon(MaterialButton button, @DrawableRes int iconResId) {
        if (iconResId == 0) {
            button.setIcon(null);
            return;
        }
        button.setIconResource(iconResId);
    }

    private static int dp(View view, int value) {
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }

    private static final class HeaderViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView title;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.settings_header_icon);
            title = itemView.findViewById(R.id.settings_header_title);
        }

        void bind(HeaderItem item) {
            bindIcon(icon, item.iconResId);
            title.setText(item.titleResId);
        }
    }

    private static final class ThemeViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final MaterialButtonToggleGroup toggleGroup;
        final MaterialButton lightButton;
        final MaterialButton darkButton;
        final MaterialButton systemButton;
        final View divider;
        MaterialButtonToggleGroup.OnButtonCheckedListener checkedListener;

        ThemeViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.settings_item_card);
            toggleGroup = itemView.findViewById(R.id.settings_theme_toggle_group);
            lightButton = itemView.findViewById(R.id.settings_theme_light);
            darkButton = itemView.findViewById(R.id.settings_theme_dark);
            systemButton = itemView.findViewById(R.id.settings_theme_system);
            divider = itemView.findViewById(R.id.settings_item_divider);
        }

        void bind(ThemeItem item, Listener listener) {
            applyGroupedCardShape(card, item);
            setDividerVisibility(divider, item);
            bindButtonIcon(lightButton, item.lightIconResId);
            bindButtonIcon(darkButton, item.darkIconResId);
            bindButtonIcon(systemButton, item.systemIconResId);
            if (checkedListener != null) {
                toggleGroup.removeOnButtonCheckedListener(checkedListener);
            }
            toggleGroup.check(getThemeButtonId(item.selectedNightMode));
            checkedListener = (group, checkedId, isChecked) -> {
                if (!isChecked || checkedId == View.NO_ID) {
                    return;
                }
                listener.onThemeSelected(getThemeValue(checkedId));
            };
            toggleGroup.addOnButtonCheckedListener(checkedListener);
        }
    }

    private static final class ChoiceViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final ImageView icon;
        final TextView title;
        final TextView summary;
        final View row;
        final View divider;

        ChoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.settings_item_card);
            icon = itemView.findViewById(R.id.settings_choice_icon);
            title = itemView.findViewById(R.id.settings_choice_title);
            summary = itemView.findViewById(R.id.settings_choice_summary);
            row = itemView.findViewById(R.id.settings_choice_row);
            divider = itemView.findViewById(R.id.settings_item_divider);
        }

        void bind(ChoiceItem item, Listener listener) {
            applyGroupedCardShape(card, item);
            setDividerVisibility(divider, item);
            bindIcon(icon, item.iconResId);
            title.setText(item.titleResId);
            summary.setText(item.summaryResId);
            row.setOnClickListener(v -> listener.onChoiceClicked(item.key));
        }
    }

    private static final class SwitchViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final ImageView icon;
        final TextView title;
        final TextView summary;
        final View row;
        final MaterialSwitch toggle;
        final View divider;

        SwitchViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.settings_item_card);
            icon = itemView.findViewById(R.id.settings_switch_icon);
            title = itemView.findViewById(R.id.settings_switch_title);
            summary = itemView.findViewById(R.id.settings_switch_summary);
            row = itemView.findViewById(R.id.settings_switch_row);
            toggle = itemView.findViewById(R.id.settings_switch);
            divider = itemView.findViewById(R.id.settings_item_divider);
        }

        void bind(SwitchItem item, Listener listener) {
            boolean hasSummary = item.summaryResId != 0;
            applyGroupedCardShape(card, item);
            setDividerVisibility(divider, item);
            bindIcon(icon, item.iconResId);
            row.setMinimumHeight(dp(row, hasSummary ? REGULAR_SWITCH_MIN_HEIGHT_DP : COMPACT_SWITCH_MIN_HEIGHT_DP));
            int verticalPadding = dp(
                    row,
                    hasSummary ? REGULAR_SWITCH_VERTICAL_PADDING_DP : COMPACT_SWITCH_VERTICAL_PADDING_DP
            );
            row.setPadding(row.getPaddingLeft(), verticalPadding, row.getPaddingRight(), verticalPadding);
            title.setText(item.titleResId);
            if (hasSummary) {
                summary.setText(item.summaryResId);
                summary.setVisibility(View.VISIBLE);
            } else {
                summary.setText(null);
                summary.setVisibility(View.GONE);
            }

            toggle.setOnCheckedChangeListener(null);
            toggle.setChecked(item.checked);
            row.setOnClickListener(v -> toggle.performClick());
            toggle.setOnCheckedChangeListener((buttonView, isChecked) ->
                    listener.onSwitchChanged(item.key, isChecked));
        }
    }

    private static int getThemeButtonId(String nightMode) {
        if (NIGHT_MODE_LIGHT.equals(nightMode)) {
            return R.id.settings_theme_light;
        }
        if (NIGHT_MODE_DARK.equals(nightMode)) {
            return R.id.settings_theme_dark;
        }
        return R.id.settings_theme_system;
    }

    private static String getThemeValue(int checkedId) {
        if (checkedId == R.id.settings_theme_light) {
            return NIGHT_MODE_LIGHT;
        }
        if (checkedId == R.id.settings_theme_dark) {
            return NIGHT_MODE_DARK;
        }
        return NIGHT_MODE_SYSTEM;
    }
}
