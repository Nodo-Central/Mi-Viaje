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

public class UsedLibrariesAdapter extends RecyclerView.Adapter<UsedLibrariesAdapter.LibraryViewHolder> {

    public interface OnLibraryActionClickListener {
        void onLicenseClick(LibraryItem item);

        void onRepoClick(LibraryItem item);

        void onDocsClick(LibraryItem item);
    }

    public static final class LibraryItem {
        private final String name;
        private final String version;
        private final String licenseName;
        private final String repoUrl;
        private final String docsUrl;
        private final String searchKeywords;

        public LibraryItem(String name,
                           String version,
                           String licenseName,
                           String repoUrl,
                           String docsUrl,
                           String searchKeywords) {
            this.name = name;
            this.version = version;
            this.licenseName = licenseName;
            this.repoUrl = repoUrl;
            this.docsUrl = docsUrl;
            this.searchKeywords = searchKeywords == null ? "" : searchKeywords;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public String getLicenseName() {
            return licenseName;
        }

        public String getRepoUrl() {
            return repoUrl;
        }

        public String getDocsUrl() {
            return docsUrl;
        }

        public String getSearchKeywords() {
            return searchKeywords;
        }
    }

    static final class LibraryViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView version;
        final TextView license;
        final View licenseButton;
        final View repoButton;
        final View docsButton;

        LibraryViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.library_item_title);
            version = itemView.findViewById(R.id.library_item_version);
            license = itemView.findViewById(R.id.library_item_license);
            licenseButton = itemView.findViewById(R.id.library_item_license_button);
            repoButton = itemView.findViewById(R.id.library_item_repo_button);
            docsButton = itemView.findViewById(R.id.library_item_docs_button);
        }
    }

    private final List<LibraryItem> items = new ArrayList<>();
    private final OnLibraryActionClickListener listener;

    public UsedLibrariesAdapter(OnLibraryActionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_used_library, parent, false);
        return new LibraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryViewHolder holder, int position) {
        LibraryItem item = items.get(position);
        holder.title.setText(item.getName());
        holder.version.setText(itemViewString(holder, R.string.about_libraries_version, item.getVersion()));
        holder.license.setText(itemViewString(holder, R.string.about_libraries_license, item.getLicenseName()));
        holder.licenseButton.setOnClickListener(v -> listener.onLicenseClick(item));
        holder.repoButton.setOnClickListener(v -> listener.onRepoClick(item));
        holder.docsButton.setOnClickListener(v -> listener.onDocsClick(item));
    }

    private String itemViewString(LibraryViewHolder holder, int resId, String value) {
        return holder.itemView.getContext().getString(resId, value);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(@NonNull List<LibraryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }
}
