package io.github.ambrazevich.think;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import io.github.ambrazevich.think.data.Settings;
import io.github.ambrazevich.think.gameutils.InAppReviewPromptStore;
import io.github.ambrazevich.think.gameutils.StorageHelper;

public class MainActivity extends AppCompatActivity {

    private InAppReviewPromptStore reviewPromptStore;
    private ActivityResultLauncher<Intent> gameLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reviewPromptStore = new InAppReviewPromptStore(this);
        gameLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    boolean meaningfulRoundCompleted = result.getResultCode() == RESULT_OK
                            && data != null
                            && data.getBooleanExtra(
                            GameActivity.EXTRA_MEANINGFUL_ROUND_COMPLETED, false);
                    if (meaningfulRoundCompleted) {
                        maybeRequestInAppReview();
                    }
                });
        
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
                    gameLauncher.launch(intent);
                }
            });
        }
    }

    private void maybeRequestInAppReview() {
        long now = System.currentTimeMillis();
        if (!reviewPromptStore.shouldRequestReview(now)) {
            return;
        }

        // A failed or quota-suppressed API call is still an attempt. The Play API deliberately
        // does not reveal whether its card was shown or whether the player submitted a review.
        reviewPromptStore.recordAttempt(now);

        final ReviewManager reviewManager;
        try {
            reviewManager = ReviewManagerFactory.create(this);
        } catch (RuntimeException unavailable) {
            return;
        }

        try {
            reviewManager.requestReviewFlow().addOnCompleteListener(this, request -> {
                if (!request.isSuccessful() || isFinishing() || isDestroyed()) {
                    return;
                }
                ReviewInfo reviewInfo = request.getResult();
                try {
                    reviewManager.launchReviewFlow(this, reviewInfo)
                            .addOnCompleteListener(this, ignored -> {
                                // Keep MainActivity open and continue normally regardless of
                                // whether Google Play displayed or completed the review card.
                            });
                } catch (RuntimeException unavailable) {
                    // Missing or unavailable Play Store must not alter the navigation flow.
                }
            });
        } catch (RuntimeException unavailable) {
            // Missing or unavailable Play Store must not alter the navigation flow.
        }
    }
}
