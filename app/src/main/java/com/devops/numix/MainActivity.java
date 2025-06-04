package com.devops.numix;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast; // Import Toast

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.devops.numix.data.Settings; // Import Settings
import com.devops.numix.gameutils.StorageHelper; // Import StorageHelper


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

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
                    Toast.makeText(MainActivity.this, "Please configure settings first (select at least one operation).", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }
}
