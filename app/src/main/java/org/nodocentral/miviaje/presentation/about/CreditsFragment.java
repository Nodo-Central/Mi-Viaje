package org.nodocentral.miviaje.presentation.about;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.databinding.FragmentCreditsBinding;

public class CreditsFragment extends Fragment {

    private FragmentCreditsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreditsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Context context = inflater.getContext();

        binding.creditsAppName.setText(context.getPackageName());
        binding.creditsAppVersion.setText(getVersionName(context) + " (" + getVersionCode(context) + ")");
        binding.creditsSupportButton.setOnClickListener(v -> openSupportLink());

        return root;
    }

    private void openSupportLink() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.support_kofi_url))));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(requireContext(), R.string.about_libraries_open_link_error, Toast.LENGTH_SHORT).show();
        }
    }

    long getVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                return pInfo.getLongVersionCode();
            } else {
                return pInfo.versionCode;
            }

        } catch (PackageManager.NameNotFoundException ignored) {
            return -1L;
        }
    }

    String getVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
            return "";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
