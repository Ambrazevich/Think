package com.challenge.think;

import android.content.Context; // Import Context
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast; // Import Toast

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.challenge.think.data.Settings; // Import Settings
import com.challenge.think.gameutils.LocaleHelper; // Import LocaleHelper
import com.challenge.think.gameutils.StorageHelper; // Import StorageHelper
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
        setContentView(R.layout.activity_main);

        Button buttonSettings = findViewById(R.id.buttonSettings);
        Button buttonResults = findViewById(R.id.buttonResults);
        Button buttonStartGame = findViewById(R.id.buttonStartGame);

        if (buttonSettings == null) Log.e(TAG, "buttonSettings not found!");
        if (buttonResults == null) Log.e(TAG, "buttonResults not found!");
        if (buttonStartGame == null) Log.e(TAG, "buttonStartGame not found!");


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
                StorageHelper storageHelper = new StorageHelper(MainActivity.this);
                Settings settings = storageHelper.loadSettings(); // loadSettings now ensures non-null return

                // settings object itself is guaranteed non-null by StorageHelper.loadSettings()
                // We only need to check if operations are selected.
                if (!settings.hasAtLeastOneOperationSelected()) {
                    Toast.makeText(MainActivity.this, R.string.configure_settings_first, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    startActivity(intent);
                }
            });
        }

        // checkAndShowRateDialog();
    }



    @Override
    public void onRatingSubmitted(int rating, @Nullable String comment) {
        // handle rating: send to server / store locally / show thank you
        Log.i("Rate", "rating=" + rating + " comment=" + comment);
        Toast.makeText(this, getString(R.string.rating_thanks, rating), Toast.LENGTH_SHORT).show();
        // a real app would probably send this to a server

        // for this demo, we'll just store that the user has rated
        getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putBoolean("has_rated", true).apply();
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
            RateDialogFragment dlg = new RateDialogFragment();
            dlg.show(getSupportFragmentManager(), "rate_dialog");
        }
    }
}
