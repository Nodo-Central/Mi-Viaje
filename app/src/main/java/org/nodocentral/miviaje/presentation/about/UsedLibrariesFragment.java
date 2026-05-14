package org.nodocentral.miviaje.presentation.about;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.databinding.FragmentUsedLibrariesBinding;
import org.nodocentral.miviaje.presentation.adapters.LicensesAdapter;
import org.nodocentral.miviaje.presentation.adapters.UsedLibrariesAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UsedLibrariesFragment extends Fragment {

    private FragmentUsedLibrariesBinding binding;
    private UsedLibrariesAdapter adapter;
    private List<UsedLibrariesAdapter.LibraryItem> allLibraries;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUsedLibrariesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        adapter = new UsedLibrariesAdapter(new UsedLibrariesAdapter.OnLibraryActionClickListener() {
            @Override
            public void onLicenseClick(UsedLibrariesAdapter.LibraryItem item) {
                showLicenseSheet(item);
            }

            @Override
            public void onRepoClick(UsedLibrariesAdapter.LibraryItem item) {
                openUrl(item.getRepoUrl());
            }

            @Override
            public void onDocsClick(UsedLibrariesAdapter.LibraryItem item) {
                openUrl(item.getDocsUrl());
            }
        });
        allLibraries = createLibraries();

        binding.librariesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.librariesRecycler.setAdapter(adapter);
        binding.librariesSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLibraries(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No-op.
            }
        });

        filterLibraries(binding.librariesSearchInput.getText());
        return root;
    }

    private void filterLibraries(CharSequence query) {
        String normalizedQuery = normalize(query == null ? "" : query.toString());
        List<UsedLibrariesAdapter.LibraryItem> filteredLibraries = new ArrayList<>();

        for (UsedLibrariesAdapter.LibraryItem item : allLibraries) {
            if (normalizedQuery.isEmpty() || matches(item, normalizedQuery)) {
                filteredLibraries.add(item);
            }
        }

        adapter.submitList(filteredLibraries);
        boolean isEmpty = filteredLibraries.isEmpty();
        binding.librariesEmptyText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.librariesRecycler.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private boolean matches(UsedLibrariesAdapter.LibraryItem item, String query) {
        return normalize(item.getName()).contains(query)
                || normalize(item.getVersion()).contains(query)
                || normalize(item.getLicenseName()).contains(query)
                || normalize(item.getSearchKeywords()).contains(query);
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).trim();
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(requireContext(), R.string.about_libraries_open_link_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void copyLicenseToClipboard(LicensesAdapter.LicenseItem item) {
        ClipboardManager clipboard = (ClipboardManager) requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return;
        }

        ClipData clip = ClipData.newPlainText(item.getName(), item.getFullText());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(requireContext(), R.string.about_licenses_copied, Toast.LENGTH_SHORT).show();
    }

    private void showLicenseSheet(UsedLibrariesAdapter.LibraryItem libraryItem) {
        LicensesAdapter.LicenseItem licenseItem = LicenseCatalog.createLicenseForLibrary(
                getResources(),
                libraryItem.getName(),
                libraryItem.getLicenseName()
        );
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_license, null, false);

        TextView title = sheetView.findViewById(R.id.license_sheet_title);
        TextView usedBy = sheetView.findViewById(R.id.license_sheet_used_by);
        TextView body = sheetView.findViewById(R.id.license_sheet_body);
        View copyButton = sheetView.findViewById(R.id.license_sheet_copy_button);

        title.setText(licenseItem.getName());
        usedBy.setText(getString(R.string.about_licenses_used_by, licenseItem.getUsedBy()));
        body.setText(licenseItem.getFullText());
        copyButton.setOnClickListener(v -> copyLicenseToClipboard(licenseItem));

        dialog.setContentView(sheetView);
        dialog.show();
    }

    private List<UsedLibrariesAdapter.LibraryItem> createLibraries() {
        List<UsedLibrariesAdapter.LibraryItem> libraries = new ArrayList<>();
        libraries.add(new UsedLibrariesAdapter.LibraryItem(
                "Mi Viaje",
                "0.3.1",
                "Apache 2.0",
                "https://github.com/Nodo-Central/Mi-Viaje",
                "https://github.com/Nodo-Central/Mi-Viaje#readme",
                "app application source code nodo central mi movilidad nfc"
        ));
        libraries.add(new UsedLibrariesAdapter.LibraryItem(
                "AndroidX",
                "1.2.1-3.7.0",
                "Apache 2.0",
                "https://github.com/androidx/androidx",
                "https://developer.android.com/jetpack/androidx/releases",
                "androidx appcompat activity constraintlayout espresso junit lifecycle livedata navigation preference recyclerview room room compiler room testing viewmodel"
        ));
        libraries.add(new UsedLibrariesAdapter.LibraryItem(
                "FlexboxLayout",
                "3.0.0",
                "Apache 2.0",
                "https://github.com/google/flexbox-layout",
                "https://github.com/google/flexbox-layout#readme",
                "google flexbox layout ui wrapping"
        ));
        libraries.add(new UsedLibrariesAdapter.LibraryItem(
                "Glide",
                "5.0.7",
                "BSD, part MIT and Apache 2.0",
                "https://github.com/bumptech/glide",
                "https://bumptech.github.io/glide/",
                "image loading bitmap cache photos"
        ));
        libraries.add(new UsedLibrariesAdapter.LibraryItem(
                "uCrop",
                "2.2.11",
                "Apache 2.0",
                "https://github.com/Yalantis/uCrop",
                "https://github.com/Yalantis/uCrop#readme",
                "image crop cropping yalantis ucrop"
        ));
        libraries.add(new UsedLibrariesAdapter.LibraryItem(
                "Gson",
                "2.14.0",
                "Apache 2.0",
                "https://github.com/google/gson",
                "https://github.com/google/gson/blob/main/UserGuide.md",
                "json serialization parsing google"
        ));
        libraries.add(new UsedLibrariesAdapter.LibraryItem(
                "RecyclerView FastScroll",
                "2.0.1",
                "Apache 2.0",
                "https://github.com/timusus/RecyclerView-FastScroll",
                "https://github.com/timusus/RecyclerView-FastScroll#readme",
                "fast scroll recycler list scrollbar"
        ));
        libraries.add(new UsedLibrariesAdapter.LibraryItem(
                "Material Components",
                "1.13.0",
                "Apache 2.0",
                "https://github.com/material-components/material-components-android",
                "https://github.com/material-components/material-components-android/blob/master/docs/getting-started.md",
                "material3 google ui components"
        ));
        libraries.add(new UsedLibrariesAdapter.LibraryItem(
                "JUnit",
                "4.13.2",
                "Eclipse Public License 1.0",
                "https://github.com/junit-team/junit4",
                "https://junit.org/junit4/",
                "test unit assertions runner"
        ));
        libraries.add(new UsedLibrariesAdapter.LibraryItem(
                "Mockito Inline",
                "5.2.0",
                "MIT",
                "https://github.com/mockito/mockito",
                "https://site.mockito.org/",
                "test mocks mocking inline"
        ));
        libraries.add(new UsedLibrariesAdapter.LibraryItem(
                "Robolectric",
                "4.16.1",
                "MIT",
                "https://github.com/robolectric/robolectric",
                "https://robolectric.org/",
                "test android unit shadows"
        ));
        return libraries;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
        allLibraries = null;
    }
}
