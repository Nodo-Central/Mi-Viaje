package org.nodocentral.miviaje.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.nodocentral.miviaje.R;

import java.util.ArrayList;
import java.util.List;

public class LicensesAdapter extends RecyclerView.Adapter<LicensesAdapter.LicenseViewHolder> {

    public interface OnLicenseActionClickListener {
        void onLicenseClick(LicenseItem item);

        void onCopyClick(LicenseItem item);
    }

    public static final class LicenseItem {
        private final String name;
        private final String preview;
        private final String fullText;
        private final String usedBy;
        private final String searchKeywords;

        public LicenseItem(String name,
                           String preview,
                           String fullText,
                           String usedBy,
                           String searchKeywords) {
            this.name = name;
            this.preview = preview;
            this.fullText = fullText;
            this.usedBy = usedBy;
            this.searchKeywords = searchKeywords == null ? "" : searchKeywords;
        }

        public String getName() {
            return name;
        }

        public String getPreview() {
            return preview;
        }

        public String getFullText() {
            return fullText;
        }

        public String getUsedBy() {
            return usedBy;
        }

        public String getSearchKeywords() {
            return searchKeywords;
        }
    }

    static final class LicenseViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView preview;
        final TextView usedBy;
        final View card;
        final View copyButton;

        LicenseViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.license_item_title);
            preview = itemView.findViewById(R.id.license_item_preview);
            usedBy = itemView.findViewById(R.id.license_item_used_by);
            card = itemView.findViewById(R.id.license_item_card);
            copyButton = itemView.findViewById(R.id.license_item_copy_button);
        }
    }

    private final List<LicenseItem> items = new ArrayList<>();
    private final OnLicenseActionClickListener listener;

    public LicensesAdapter(OnLicenseActionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LicenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_license, parent, false);
        return new LicenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LicenseViewHolder holder, int position) {
        LicenseItem item = items.get(position);
        holder.title.setText(item.getName());
        holder.preview.setText(item.getPreview());
        holder.usedBy.setText(itemViewString(holder, R.string.about_licenses_used_by, item.getUsedBy()));
        holder.card.setOnClickListener(v -> listener.onLicenseClick(item));
        holder.copyButton.setOnClickListener(v -> listener.onCopyClick(item));
    }

    private String itemViewString(LicenseViewHolder holder, int resId, String value) {
        return holder.itemView.getContext().getString(resId, value);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(@NonNull List<LicenseItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }
}
