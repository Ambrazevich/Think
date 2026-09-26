package io.github.ambrazevich.think;

import android.os.Bundle;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import io.github.ambrazevich.think.gameutils.AppLanguageManager;
import io.github.ambrazevich.think.gameutils.EdgeToEdgeInsets;
import io.github.ambrazevich.think.data.OperationType;
import io.github.ambrazevich.think.data.Settings;
import io.github.ambrazevich.think.gameutils.StorageHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    private static final String STATE_SCROLL_Y = "settings_scroll_y";
    private static final String STATE_DIFFICULTY = "settings_difficulty";
    private static final String STATE_TIME = "settings_time";
    private static final String STATE_OPERATIONS = "settings_operations";
    private static final String STATE_DARK_MODE = "settings_dark_mode";

    // --- Using NumberPickers instead of Spinners ---
    private NumberPicker numberPickerDifficulty;
    private NumberPicker numberPickerTime;
    private NumberPicker numberPickerLanguage;
    private ScrollView settingsScroll;

    private SwitchMaterial switchAddition, switchSubtraction, switchMultiplication, switchDivision, switchPower,
            switchSquareRoot, switchCommonFractions, switchDecimalFractions, switchDarkMode;

    private StorageHelper storageHelper;
    private Settings currentSettings;
    private String[] languageCodes;
    private boolean updatingLanguagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        EdgeToEdgeInsets.apply(this, findViewById(R.id.settingsRoot));

        storageHelper = new StorageHelper(this);
        currentSettings = storageHelper.loadSettings();
        
        // Apply theme based on settings (if not already applied by system or previous activity)
        // Note: AppCompatDelegate.setDefaultNightMode is usually process-wide.
        // We ensure the switch matches the actual setting.

        languageCodes = getResources().getStringArray(R.array.language_codes);

        // Initialize views
        numberPickerDifficulty = findViewById(R.id.numberPickerDifficulty);
        numberPickerTime = findViewById(R.id.numberPickerTime);
        numberPickerLanguage = findViewById(R.id.numberPickerLanguage);
        settingsScroll = findViewById(R.id.settingsScroll);
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
        restoreUiState(savedInstanceState);

        numberPickerLanguage.setOnValueChangedListener(
                (picker, oldValue, newValue) -> handleLanguageSelection(newValue));

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentSettings.setDarkMode(isChecked);
            if (!persistSettingsFromUI(false)) {
                storageHelper.saveSettings(currentSettings);
            }
            
            int mode = isChecked ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode);
            // Activity will recreate automatically
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!persistSettingsFromUI(true)) {
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
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
        setLanguagePickerValue(findLanguageIndex(currentLang));

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

    private int findLanguageIndex(String languageCode) {
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(languageCode)) return i;
        }
        return 0;
    }

    private void setLanguagePickerValue(int value) {
        updatingLanguagePicker = true;
        numberPickerLanguage.setValue(value);
        updatingLanguagePicker = false;
    }

    void handleLanguageSelection(int newValue) {
        if (updatingLanguagePicker) return;

        String selectedLanguage = languageCodes[newValue];
        if (selectedLanguage.equals(AppLanguageManager.getSelectedLanguage())) return;

        persistSettingsBeforeLocaleChange();
        AppLanguageManager.setSelectedLanguage(selectedLanguage);
    }

    private void restoreUiState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;

        numberPickerDifficulty.setValue(savedInstanceState.getInt(
                STATE_DIFFICULTY, numberPickerDifficulty.getValue()));
        numberPickerTime.setValue(savedInstanceState.getInt(
                STATE_TIME, numberPickerTime.getValue()));

        boolean[] operationStates = savedInstanceState.getBooleanArray(STATE_OPERATIONS);
        if (operationStates != null && operationStates.length == 8) {
            switchAddition.setChecked(operationStates[0]);
            switchSubtraction.setChecked(operationStates[1]);
            switchMultiplication.setChecked(operationStates[2]);
            switchDivision.setChecked(operationStates[3]);
            switchPower.setChecked(operationStates[4]);
            switchSquareRoot.setChecked(operationStates[5]);
            switchCommonFractions.setChecked(operationStates[6]);
            switchDecimalFractions.setChecked(operationStates[7]);
        }
        switchDarkMode.setChecked(savedInstanceState.getBoolean(
                STATE_DARK_MODE, switchDarkMode.isChecked()));

        int scrollY = savedInstanceState.getInt(STATE_SCROLL_Y, 0);
        settingsScroll.post(() -> settingsScroll.scrollTo(0, scrollY));
    }

    private boolean persistSettingsFromUI(boolean showValidationError) {
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

        copyBasicSettingsFromUI();
        currentSettings.setEnabledOperations(enabledOps);
        storageHelper.saveSettings(currentSettings);
        Log.d(TAG, "Settings saved successfully.");
        return true;
    }

    private void persistSettingsBeforeLocaleChange() {
        if (persistSettingsFromUI(false)) return;

        // Keep the last valid operation set, but do not lose unrelated wheel/switch changes.
        copyBasicSettingsFromUI();
        storageHelper.saveSettings(currentSettings);
    }

    private void copyBasicSettingsFromUI() {
        int timeValue = numberPickerTime.getValue();
        long roundTimeMillis = timeValue == 2 ? 0L : (long) (timeValue + 1) * 60_000L;

        currentSettings.setDifficultyLevel(
                Settings.Difficulty.values()[numberPickerDifficulty.getValue()]);
        currentSettings.setRoundTimeMillis(roundTimeMillis);
        currentSettings.setDarkMode(switchDarkMode.isChecked());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SCROLL_Y, settingsScroll.getScrollY());
        outState.putInt(STATE_DIFFICULTY, numberPickerDifficulty.getValue());
        outState.putInt(STATE_TIME, numberPickerTime.getValue());
        outState.putBooleanArray(STATE_OPERATIONS, new boolean[]{
                switchAddition.isChecked(),
                switchSubtraction.isChecked(),
                switchMultiplication.isChecked(),
                switchDivision.isChecked(),
                switchPower.isChecked(),
                switchSquareRoot.isChecked(),
                switchCommonFractions.isChecked(),
                switchDecimalFractions.isChecked()
        });
        outState.putBoolean(STATE_DARK_MODE, switchDarkMode.isChecked());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        persistSettingsFromUI(false);
    }

}
