package org.nodocentral.miviaje.presentation.about;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import org.nodocentral.miviaje.databinding.FragmentLicensesBinding;
import org.nodocentral.miviaje.presentation.adapters.LicensesAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LicensesFragment extends Fragment {

    private FragmentLicensesBinding binding;
    private LicensesAdapter adapter;
    private List<LicensesAdapter.LicenseItem> allLicenses;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLicensesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        adapter = new LicensesAdapter(new LicensesAdapter.OnLicenseActionClickListener() {
            @Override
            public void onLicenseClick(LicensesAdapter.LicenseItem item) {
                showLicenseSheet(item);
            }

            @Override
            public void onCopyClick(LicensesAdapter.LicenseItem item) {
                copyLicenseToClipboard(item);
            }
        });
        allLicenses = LicenseCatalog.createLicenses(getResources());

        binding.licensesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.licensesRecycler.setAdapter(adapter);
        binding.licensesSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLicenses(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No-op.
            }
        });

        filterLicenses(binding.licensesSearchInput.getText());
        return root;
    }

    private void filterLicenses(CharSequence query) {
        String normalizedQuery = normalize(query == null ? "" : query.toString());
        List<LicensesAdapter.LicenseItem> filteredLicenses = new ArrayList<>();

        for (LicensesAdapter.LicenseItem item : allLicenses) {
            if (normalizedQuery.isEmpty() || matches(item, normalizedQuery)) {
                filteredLicenses.add(item);
            }
        }

        adapter.submitList(filteredLicenses);
        boolean isEmpty = filteredLicenses.isEmpty();
        binding.licensesEmptyText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.licensesRecycler.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private boolean matches(LicensesAdapter.LicenseItem item, String query) {
        return normalize(item.getName()).contains(query)
                || normalize(item.getPreview()).contains(query)
                || normalize(item.getUsedBy()).contains(query)
                || normalize(item.getSearchKeywords()).contains(query)
                || normalize(item.getFullText()).contains(query);
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).trim();
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

    private void showLicenseSheet(LicensesAdapter.LicenseItem item) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_license, null, false);

        TextView title = sheetView.findViewById(R.id.license_sheet_title);
        TextView usedBy = sheetView.findViewById(R.id.license_sheet_used_by);
        TextView body = sheetView.findViewById(R.id.license_sheet_body);
        View copyButton = sheetView.findViewById(R.id.license_sheet_copy_button);

        title.setText(item.getName());
        usedBy.setText(getString(R.string.about_licenses_used_by, item.getUsedBy()));
        body.setText(item.getFullText());
        copyButton.setOnClickListener(v -> copyLicenseToClipboard(item));

        dialog.setContentView(sheetView);
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
        allLicenses = null;
    }
}
