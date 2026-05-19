package org.nodocentral.miviaje.presentation;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.core.view.WindowInsetsCompat;

import org.nodocentral.miviaje.R;
import org.nodocentral.miviaje.databinding.ActivityHelpBinding;

public class HelpActivity extends BaseActivity {
    private ActivityHelpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setToolbar(true);
        binding.helpSupportButton.setOnClickListener(v -> openSupportLink());
        applyInsetsToPadding(
                binding.helpScroll,
                WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.displayCutout(),
                true,
                false,
                true,
                true
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSupportLink() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.support_kofi_url))));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.about_libraries_open_link_error, Toast.LENGTH_SHORT).show();
        }
    }
}
