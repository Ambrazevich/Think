package io.github.ambrazevich.think;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.github.ambrazevich.think.data.Settings;
import io.github.ambrazevich.think.gameutils.StorageHelper;

public class MainActivity extends AppCompatActivity {

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
    }
}
