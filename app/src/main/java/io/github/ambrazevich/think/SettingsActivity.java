package io.github.ambrazevich.think;

import android.os.Bundle;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import io.github.ambrazevich.think.gameutils.AppLanguageManager;
import io.github.ambrazevich.think.data.OperationType;
import io.github.ambrazevich.think.data.Settings;
import io.github.ambrazevich.think.gameutils.StorageHelper;
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
            switchSquareRoot, switchCommonFractions, switchDecimalFractions, switchDarkMode;

    private StorageHelper storageHelper;
    private Settings currentSettings;
    private String[] languageCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        storageHelper = new StorageHelper(this);
        currentSettings = storageHelper.loadSettings();
        
        // Apply theme based on settings (if not already applied by system or previous activity)
        // Note: AppCompatDelegate.setDefaultNightMode is usually process-wide.
        // We ensure the switch matches the actual setting.

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
        switchDarkMode = findViewById(R.id.switchDarkMode);

        setupNumberPickers();
        loadSettingsToUI();

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentSettings.setDarkMode(isChecked);
            if (!persistSettingsFromUI(false)) {
                storageHelper.saveSettings(currentSettings);
            }
            
            int mode = isChecked ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode);
            // Activity will recreate automatically
        });

    }

    private void setupNumberPickers() {
        // Difficulty NumberPicker
        numberPickerDifficulty.setMinValue(0);
        numberPickerDifficulty.setMaxValue(Settings.Difficulty.values().length - 1);
        numberPickerDifficulty.setDisplayedValues(null); // Reset first
        numberPickerDifficulty.setDisplayedValues(getResources().getStringArray(R.array.difficulty_levels));
        numberPickerDifficulty.setWrapSelectorWheel(false);

        // Time NumberPicker
        numberPickerTime.setMinValue(0);
        numberPickerTime.setMaxValue(2);
        numberPickerTime.setDisplayedValues(null); // Reset first
        numberPickerTime.setDisplayedValues(getResources().getStringArray(R.array.time_options));
        numberPickerTime.setWrapSelectorWheel(false);

        // Language NumberPicker
        numberPickerLanguage.setMinValue(0);
        numberPickerLanguage.setMaxValue(languageCodes.length - 1);
        numberPickerLanguage.setDisplayedValues(null); // Reset first
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
        String currentLang = AppLanguageManager.getSelectedLanguage();
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
        
        switchDarkMode.setChecked(currentSettings.isDarkMode());
    }

    private boolean persistSettingsFromUI(boolean showValidationError) {
        int timeValue = numberPickerTime.getValue();
        long roundTimeMillis = timeValue == 2 ? 0L : (long) (timeValue + 1) * 60_000L;

        Set<OperationType> enabledOps = new HashSet<>();
        if (switchAddition.isChecked()) enabledOps.add(OperationType.ADDITION);
        if (switchSubtraction.isChecked()) enabledOps.add(OperationType.SUBTRACTION);
        if (switchMultiplication.isChecked()) enabledOps.add(OperationType.MULTIPLICATION);
        if (switchDivision.isChecked()) enabledOps.add(OperationType.DIVISION);
        if (switchPower.isChecked()) enabledOps.add(OperationType.POWER);
        if (switchSquareRoot.isChecked()) enabledOps.add(OperationType.SQUARE_ROOT);
        if (switchCommonFractions.isChecked()) enabledOps.add(OperationType.COMMON_FRACTIONS);
        if (switchDecimalFractions.isChecked()) enabledOps.add(OperationType.DECIMAL_FRACTIONS);

        if (enabledOps.isEmpty()) {
            if (showValidationError) {
                Toast.makeText(this, R.string.error_select_operation, Toast.LENGTH_LONG).show();
            }
            return false;
        }

        currentSettings.setDifficultyLevel(Settings.Difficulty.values()[numberPickerDifficulty.getValue()]);
        currentSettings.setRoundTimeMillis(roundTimeMillis);
        currentSettings.setEnabledOperations(enabledOps);
        currentSettings.setDarkMode(switchDarkMode.isChecked());
        storageHelper.saveSettings(currentSettings);
        Log.d(TAG, "Settings saved successfully.");
        return true;
    }

    private boolean applySelectedLanguageIfChanged() {
        String selectedLanguage = languageCodes[numberPickerLanguage.getValue()];
        if (selectedLanguage.equals(AppLanguageManager.getSelectedLanguage())) return false;

        AppLanguageManager.setSelectedLanguage(selectedLanguage);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        persistSettingsFromUI(false);
    }

    @Override
    public void onBackPressed() {
        if (!persistSettingsFromUI(true)) {
            return;
        }
        if (applySelectedLanguageIfChanged()) {
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
