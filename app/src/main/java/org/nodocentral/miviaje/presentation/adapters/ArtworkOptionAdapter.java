package org.nodocentral.miviaje.presentation.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.data.artwork.CardArtworkResolver;
import org.nodocentral.miviaje.domain.artwork.Artwork;

import java.util.ArrayList;
import java.util.List;

public final class ArtworkOptionAdapter extends RecyclerView.Adapter<ArtworkOptionAdapter.ViewHolder> {
    public enum OptionType {
        DEFAULT,
        BUILTIN,
        IMPORTED
    }

    public interface Listener {
        void onArtworkSelected(ArtworkOption option);

        void onImportedArtworkMenuClicked(View anchor, ArtworkOption option);
    }

    public static final class ArtworkOption {
        public final OptionType type;
        public final String artworkRef;
        public final String title;
        public final Artwork artwork;
        public final int previewDrawableResId;
        public final int usageCount;
        public final boolean selected;

        public ArtworkOption(OptionType type,
                             String artworkRef,
                             String title,
                             Artwork artwork,
                             @DrawableRes int previewDrawableResId,
                             int usageCount,
                             boolean selected) {
            this.type = type;
            this.artworkRef = artworkRef;
            this.title = title;
            this.artwork = artwork;
            this.previewDrawableResId = previewDrawableResId;
            this.usageCount = usageCount;
            this.selected = selected;
        }

        public boolean isImported() {
            return type == OptionType.IMPORTED && artwork != null;
        }
    }

    private final List<ArtworkOption> options = new ArrayList<>();
    private final Listener listener;

    public ArtworkOptionAdapter(Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artwork_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ArtworkOption option = options.get(position);
        holder.bind(option, listener);
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public void submitList(List<ArtworkOption> newOptions) {
        options.clear();
        if (newOptions != null) {
            options.addAll(newOptions);
        }
        notifyDataSetChanged();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final FrameLayout preview;
        final TextView title;
        final TextView usage;
        final TextView selectedBadge;
        final ImageButton menuButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.artwork_option_card);
            preview = itemView.findViewById(R.id.artwork_option_preview);
            title = itemView.findViewById(R.id.artwork_option_title);
            usage = itemView.findViewById(R.id.artwork_option_usage);
            selectedBadge = itemView.findViewById(R.id.artwork_option_selected_badge);
            menuButton = itemView.findViewById(R.id.artwork_option_menu);
        }

        void bind(ArtworkOption option, Listener listener) {
            Context context = itemView.getContext();
            title.setText(option.title);
            bindUsage(context, option);
            bindSelection(context, option);
            bindPreview(context, option);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onArtworkSelected(option);
                }
            });

            menuButton.setVisibility(option.isImported() ? View.VISIBLE : View.INVISIBLE);
            menuButton.setOnClickListener(v -> {
                if (listener != null && option.isImported()) {
                    listener.onImportedArtworkMenuClicked(v, option);
                }
            });
        }

        private void bindUsage(Context context, ArtworkOption option) {
            if (!option.isImported()) {
                usage.setText(null);
                usage.setVisibility(View.GONE);
                return;
            }
            usage.setText(context.getString(R.string.artwork_usage_count, option.usageCount));
            usage.setVisibility(View.VISIBLE);
        }

        private void bindSelection(Context context, ArtworkOption option) {
            int selectedColor = MaterialColors.getColor(card, androidx.appcompat.R.attr.colorPrimary, 0);
            int outlineColor = MaterialColors.getColor(card, com.google.android.material.R.attr.colorOutlineVariant, 0);
            int strokeWidth = dp(context, option.selected ? 2 : 1);
            card.setStrokeWidth(strokeWidth);
            card.setStrokeColor(option.selected ? selectedColor : outlineColor);
            selectedBadge.setVisibility(option.selected ? View.VISIBLE : View.GONE);
            selectedBadge.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
        }

        private void bindPreview(Context context, ArtworkOption option) {
            int fallbackColor = MaterialColors.getColor(preview, com.google.android.material.R.attr.colorSurfaceContainerHigh, 0);
            preview.setBackgroundColor(fallbackColor);

            if (option.isImported()) {
                CardArtworkResolver.applyCustomBackground(context, option.artworkRef, option.artwork, preview);
                return;
            }
            if (option.previewDrawableResId != 0) {
                preview.setBackgroundResource(option.previewDrawableResId);
            }
        }

        private static int dp(Context context, int value) {
            return Math.round(value * context.getResources().getDisplayMetrics().density);
        }
    }
}
