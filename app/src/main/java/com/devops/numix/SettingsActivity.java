package com.devops.numix;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.devops.numix.data.OperationType;
import com.devops.numix.data.Settings;
import com.devops.numix.gameutils.StorageHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    private Spinner spinnerDifficulty;
    private Spinner spinnerTime;
    private SwitchMaterial switchAddition, switchSubtraction, switchMultiplication, switchDivision, switchPower, switchFractions;

    private StorageHelper storageHelper;
    private Settings currentSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        storageHelper = new StorageHelper(this);
        currentSettings = storageHelper.loadSettings();
        if (currentSettings.getEnabledOperations() == null) { // Defensive null check
            currentSettings.setEnabledOperations(new HashSet<>());
        }


        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerTime = findViewById(R.id.spinnerTime);
        switchAddition = findViewById(R.id.switchAddition);
        switchSubtraction = findViewById(R.id.switchSubtraction);
        switchMultiplication = findViewById(R.id.switchMultiplication);
        switchDivision = findViewById(R.id.switchDivision);
        switchPower = findViewById(R.id.switchPower);
        switchFractions = findViewById(R.id.switchFractions);

        setupSpinners();
        loadSettingsToUI();
    }

    private void setupSpinners() {
        // Difficulty Spinner
        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_levels, android.R.layout.simple_spinner_item);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(difficultyAdapter);

        // Time Spinner
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this,
                R.array.time_options, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(timeAdapter);
    }

    private void loadSettingsToUI() {
        // Set Difficulty
        if (currentSettings.getDifficultyLevel() != null) {
            switch (currentSettings.getDifficultyLevel()) {
                case EASY: spinnerDifficulty.setSelection(0); break;
                case MEDIUM: spinnerDifficulty.setSelection(1); break;
                case HARD: spinnerDifficulty.setSelection(2); break;
                default: spinnerDifficulty.setSelection(0); // Default to Easy
            }
        } else {
            spinnerDifficulty.setSelection(0); // Default to Easy if null
        }


        // Set Time
        long timeMillis = currentSettings.getRoundTimeMillis();
        if (timeMillis == 60000) spinnerTime.setSelection(0); // 1 min
        else if (timeMillis == 120000) spinnerTime.setSelection(1); // 2 min
        else if (timeMillis == 180000) spinnerTime.setSelection(2); // 3 min
        else if (timeMillis == 240000) spinnerTime.setSelection(3); // 4 min
        else if (timeMillis == 300000) spinnerTime.setSelection(4); // 5 min
        else if (timeMillis == 0) spinnerTime.setSelection(5); // Endless
        else spinnerTime.setSelection(0); // Default to 1 min if unknown

        // Set Operation Switches
        Set<OperationType> ops = currentSettings.getEnabledOperations();
        if (ops == null) ops = new HashSet<>(); // Ensure ops is not null

        switchAddition.setChecked(ops.contains(OperationType.ADDITION));
        switchSubtraction.setChecked(ops.contains(OperationType.SUBTRACTION));
        switchMultiplication.setChecked(ops.contains(OperationType.MULTIPLICATION));
        switchDivision.setChecked(ops.contains(OperationType.DIVISION));
        switchPower.setChecked(ops.contains(OperationType.POWER));
        switchFractions.setChecked(ops.contains(OperationType.FRACTIONS)); // This is the main category switch
    }

    private boolean saveSettingsFromUI() { // Returns true if save was successful (ops selected)
        // Get Difficulty
        // Using direct string comparison from array resource for robustness
        String selectedDifficulty = spinnerDifficulty.getSelectedItem().toString();
        String[] difficultyLevels = getResources().getStringArray(R.array.difficulty_levels);

        if (selectedDifficulty.equals(difficultyLevels[0])) currentSettings.setDifficultyLevel(Settings.Difficulty.EASY);
        else if (selectedDifficulty.equals(difficultyLevels[1])) currentSettings.setDifficultyLevel(Settings.Difficulty.MEDIUM);
        else if (selectedDifficulty.equals(difficultyLevels[2])) currentSettings.setDifficultyLevel(Settings.Difficulty.HARD);
        else currentSettings.setDifficultyLevel(Settings.Difficulty.EASY); // Default


        // Get Time
        int timePosition = spinnerTime.getSelectedItemPosition();
        if (timePosition == 5) currentSettings.setRoundTimeMillis(0); // Endless (index 5)
        else currentSettings.setRoundTimeMillis((long)(timePosition + 1) * 60000L); // (0+1)*60k, (1+1)*60k etc.

        // Get Operations
        Set<OperationType> enabledOps = new HashSet<>();
        if (switchAddition.isChecked()) enabledOps.add(OperationType.ADDITION);
        if (switchSubtraction.isChecked()) enabledOps.add(OperationType.SUBTRACTION);
        if (switchMultiplication.isChecked()) enabledOps.add(OperationType.MULTIPLICATION);
        if (switchDivision.isChecked()) enabledOps.add(OperationType.DIVISION);
        if (switchPower.isChecked()) enabledOps.add(OperationType.POWER);
        if (switchFractions.isChecked()) enabledOps.add(OperationType.FRACTIONS); // Main category switch

        currentSettings.setEnabledOperations(enabledOps);

        if (!currentSettings.hasAtLeastOneOperationSelected()) {
            Toast.makeText(this, R.string.error_select_operation, Toast.LENGTH_LONG).show();
            return false; // Indicate save failed due to no operations
        }
        storageHelper.saveSettings(currentSettings);
        return true; // Save successful
    }


    @Override
    protected void onPause() {
        super.onPause();
        saveSettingsFromUI(); // Attempt to save settings when activity is paused/exited
    }

    @Override
    public void onBackPressed() {
        if (saveSettingsFromUI()) { // If settings saved successfully (meaning at least one op selected)
            super.onBackPressed();
        }
        // If saveSettingsFromUI() returned false, it means no operation was selected,
        // and a Toast was already shown. We don't call super.onBackPressed() to keep the user here.
    }
}
