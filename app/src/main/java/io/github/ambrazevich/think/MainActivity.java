package io.github.ambrazevich.think;

import android.content.Context; // Import Context
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast; // Import Toast

import androidx.appcompat.app.AppCompatActivity;

import io.github.ambrazevich.think.data.Settings; // Import Settings
import io.github.ambrazevich.think.gameutils.LocaleHelper; // Import LocaleHelper
import io.github.ambrazevich.think.gameutils.StorageHelper; // Import StorageHelper
import androidx.annotation.Nullable;


public class MainActivity extends AppCompatActivity implements RateDialogFragment.RateDialogListener {
    private static final String TAG = "MainActivity";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply Night Mode based on settings
        StorageHelper storageHelper = new StorageHelper(this);
        Settings settings = storageHelper.loadSettings();
        if (settings != null) {
            int mode = settings.isDarkMode() ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode);
        }

        setContentView(R.layout.activity_main);

        Button buttonSettings = findViewById(R.id.buttonSettings);
        Button buttonResults = findViewById(R.id.buttonResults);
        Button buttonStartGame = findViewById(R.id.buttonStartGame);
        Button buttonRateFeedback = findViewById(R.id.buttonRateFeedback);

        if (buttonSettings == null) Log.e(TAG, "buttonSettings not found!");
        if (buttonResults == null) Log.e(TAG, "buttonResults not found!");
        if (buttonStartGame == null) Log.e(TAG, "buttonStartGame not found!");
        if (buttonRateFeedback == null) Log.e(TAG, "buttonRateFeedback not found!");


        if (buttonSettings != null) {
            buttonSettings.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            });
        }

        if (buttonResults != null) {
            buttonResults.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ResultsActivity.class);
                startActivity(intent);
            });
        }

        if (buttonStartGame != null) {
            buttonStartGame.setOnClickListener(v -> {
                StorageHelper sh = new StorageHelper(MainActivity.this);
                Settings s = sh.loadSettings(); // loadSettings now ensures non-null return

                // settings object itself is guaranteed non-null by StorageHelper.loadSettings()
                // We only need to check if operations are selected.
                if (!s.hasAtLeastOneOperationSelected()) {
                    Toast.makeText(MainActivity.this, R.string.configure_settings_first, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    startActivity(intent);
                }
            });
        }

        if (buttonRateFeedback != null) {
            buttonRateFeedback.setOnClickListener(v -> showRateDialog());
        }

        checkAndShowRateDialog();
    }



    @Override
    public void onRatingSubmitted(int rating, @Nullable String comment) {
        Log.i("Rate", "rating=" + rating + " comment=" + comment);
        if (rating > 0) {
            Toast.makeText(this, getString(R.string.rating_thanks, rating), Toast.LENGTH_SHORT).show();
        }
        getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putBoolean("has_rated", true).apply();

        Intent feedbackIntent = new Intent(Intent.ACTION_SEND);
        feedbackIntent.setType("text/plain");
        feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_share_title));
        feedbackIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.feedback_share_text, rating, comment == null ? "" : comment));
        try {
            startActivity(Intent.createChooser(feedbackIntent, getString(R.string.feedback_share_title)));
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, R.string.feedback_share_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRatingCancelled() {
        // optional
    }

    private void checkAndShowRateDialog() {
        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean hasRated = prefs.getBoolean("has_rated", false);
        if (hasRated) return;

        long installTime = prefs.getLong("install_time", 0);
        if (installTime == 0) {
            // first time running, store install time
            prefs.edit().putLong("install_time", System.currentTimeMillis()).apply();
            return; // don't show on first run
        }

        // 12 hours in millis
        long twelveHours = 12 * 60 * 60 * 1000;

        if (System.currentTimeMillis() - installTime >= twelveHours) {
            showRateDialog();
        }
    }

    private void showRateDialog() {
        if (getSupportFragmentManager().findFragmentByTag("rate_dialog") != null) return;
        new RateDialogFragment().show(getSupportFragmentManager(), "rate_dialog");
    }
}
