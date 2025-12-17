package com.challenge.think;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.NumberPicker; // Import NumberPicker
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.challenge.think.gameutils.LocaleHelper;
import android.content.Context;
import android.content.Intent;
import com.challenge.think.data.OperationType;
import com.challenge.think.data.Settings;
import com.challenge.think.gameutils.StorageHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    // --- Using NumberPickers instead of Spinners ---
    private NumberPicker numberPickerDifficulty;
    private NumberPicker numberPickerTime;
    private NumberPicker numberPickerLanguage; // Add Language Picker

    private SwitchMaterial switchAddition, switchSubtraction, switchMultiplication, switchDivision, switchPower,
            switchSquareRoot, switchCommonFractions, switchDecimalFractions;

    private StorageHelper storageHelper;
    private Settings currentSettings;
    private String[] languageCodes;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private final CompoundButton.OnCheckedChangeListener basicOperationListener = (buttonView, isChecked) -> {
        updateFractionSwitchesState();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        storageHelper = new StorageHelper(this);
        currentSettings = storageHelper.loadSettings();
        languageCodes = getResources().getStringArray(R.array.language_codes);

        // Initialize views
        numberPickerDifficulty = findViewById(R.id.numberPickerDifficulty);
        numberPickerTime = findViewById(R.id.numberPickerTime);
        numberPickerLanguage = findViewById(R.id.numberPickerLanguage); // Init
        switchAddition = findViewById(R.id.switchAddition);
        switchSubtraction = findViewById(R.id.switchSubtraction);
        switchMultiplication = findViewById(R.id.switchMultiplication);
        switchDivision = findViewById(R.id.switchDivision);
        switchPower = findViewById(R.id.switchPower);
        switchSquareRoot = findViewById(R.id.switchSquareRoot);
        switchCommonFractions = findViewById(R.id.switchCommonFractions);
        switchDecimalFractions = findViewById(R.id.switchDecimalFractions);

        setupNumberPickers();
        loadSettingsToUI();

        // Attach listener after loading initial state
        switchAddition.setOnCheckedChangeListener(basicOperationListener);
        switchSubtraction.setOnCheckedChangeListener(basicOperationListener);
        switchMultiplication.setOnCheckedChangeListener(basicOperationListener);
        switchDivision.setOnCheckedChangeListener(basicOperationListener);

        // Set the initial enabled/disabled state for fraction switches
        updateFractionSwitchesState();
    }

    private void updateFractionSwitchesState() {
        boolean anyBasicOpSelected = switchAddition.isChecked() ||
                switchSubtraction.isChecked() ||
                switchMultiplication.isChecked() ||
                switchDivision.isChecked();

        switchCommonFractions.setEnabled(anyBasicOpSelected);
        switchDecimalFractions.setEnabled(anyBasicOpSelected);

        if (!anyBasicOpSelected) {
            switchCommonFractions.setChecked(false);
            switchDecimalFractions.setChecked(false);
        }
    }

    private void setupNumberPickers() {
        // Difficulty NumberPicker
        numberPickerDifficulty.setMinValue(0);
        numberPickerDifficulty.setMaxValue(Settings.Difficulty.values().length - 1);
        numberPickerDifficulty.setDisplayedValues(getResources().getStringArray(R.array.difficulty_levels));
        numberPickerDifficulty.setWrapSelectorWheel(false);

        // Time NumberPicker
        numberPickerTime.setMinValue(0);
        numberPickerTime.setMaxValue(2);
        numberPickerTime.setDisplayedValues(getResources().getStringArray(R.array.time_options));
        numberPickerTime.setWrapSelectorWheel(false);

        // Language NumberPicker
        numberPickerLanguage.setMinValue(0);
        numberPickerLanguage.setMaxValue(languageCodes.length - 1);
        numberPickerLanguage.setDisplayedValues(getResources().getStringArray(R.array.language_options));
        numberPickerLanguage.setWrapSelectorWheel(false);
    }

    private void loadSettingsToUI() {
        // Load settings into NumberPickers
        if (currentSettings.getDifficultyLevel() != null) {
            numberPickerDifficulty.setValue(currentSettings.getDifficultyLevel().ordinal());
        }

        long timeMillis = currentSettings.getRoundTimeMillis();
        if (timeMillis == 0L) {
            numberPickerTime.setValue(2); // Endless
        } else {
            int minutes = (int) (timeMillis / 60000L);
            if (minutes > 2) minutes = 2; 
            numberPickerTime.setValue(minutes - 1);
        }

        // Load Language
        String currentLang = LocaleHelper.getLanguage(this);
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLang)) {
                numberPickerLanguage.setValue(i);
                break;
            }
        }

        Set<OperationType> ops = currentSettings.getEnabledOperations();
        if (ops == null) ops = new HashSet<>();

        switchAddition.setChecked(ops.contains(OperationType.ADDITION));
        switchSubtraction.setChecked(ops.contains(OperationType.SUBTRACTION));
        switchMultiplication.setChecked(ops.contains(OperationType.MULTIPLICATION));
        switchDivision.setChecked(ops.contains(OperationType.DIVISION));
        switchPower.setChecked(ops.contains(OperationType.POWER));
        switchSquareRoot.setChecked(ops.contains(OperationType.SQUARE_ROOT));
        switchCommonFractions.setChecked(ops.contains(OperationType.COMMON_FRACTIONS));
        switchDecimalFractions.setChecked(ops.contains(OperationType.DECIMAL_FRACTIONS));
    }

    private boolean saveSettingsFromUI() {
        // Save settings from NumberPickers
        currentSettings.setDifficultyLevel(Settings.Difficulty.values()[numberPickerDifficulty.getValue()]);

        int timeValue = numberPickerTime.getValue();
        if (timeValue == 2) { // Index 2 is endless
            currentSettings.setRoundTimeMillis(0);
        } else {
            currentSettings.setRoundTimeMillis((long)(timeValue + 1) * 60000L);
        }

        Set<OperationType> enabledOps = new HashSet<>();
        if (switchAddition.isChecked()) enabledOps.add(OperationType.ADDITION);
        if (switchSubtraction.isChecked()) enabledOps.add(OperationType.SUBTRACTION);
        if (switchMultiplication.isChecked()) enabledOps.add(OperationType.MULTIPLICATION);
        if (switchDivision.isChecked()) enabledOps.add(OperationType.DIVISION);
        if (switchPower.isChecked()) enabledOps.add(OperationType.POWER);
        if (switchSquareRoot.isChecked()) enabledOps.add(OperationType.SQUARE_ROOT);
        if (switchCommonFractions.isChecked()) enabledOps.add(OperationType.COMMON_FRACTIONS);
        if (switchDecimalFractions.isChecked()) enabledOps.add(OperationType.DECIMAL_FRACTIONS);

        currentSettings.setEnabledOperations(enabledOps);

        if (!currentSettings.hasAtLeastOneOperationSelected()) {
            Toast.makeText(this, R.string.error_select_operation, Toast.LENGTH_LONG).show();
            return false;
        }
        storageHelper.saveSettings(currentSettings);
        Log.d(TAG, "Settings saved successfully.");

        // Check for language change
        int selectedLangIndex = numberPickerLanguage.getValue();
        String selectedLang = languageCodes[selectedLangIndex];
        if (!selectedLang.equals(LocaleHelper.getLanguage(this))) {
            LocaleHelper.setLocale(this, selectedLang);
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSettingsFromUI();
    }

    @Override
    public void onBackPressed() {
        if (saveSettingsFromUI()) {
            super.onBackPressed();
        }
    }
}
