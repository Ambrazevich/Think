package com.devops.numix;

import android.os.Bundle;
import android.util.Log;
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
    private static final String TAG = "SettingsActivity";

    private Spinner spinnerDifficulty;
    private Spinner spinnerTime;
    private SwitchMaterial switchAddition, switchSubtraction, switchMultiplication, switchDivision, switchPower,
            switchSquareRoot, switchCommonFractions, switchDecimalFractions;

    private StorageHelper storageHelper;
    private Settings currentSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        storageHelper = new StorageHelper(this);
        currentSettings = storageHelper.loadSettings();

        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerTime = findViewById(R.id.spinnerTime);
        switchAddition = findViewById(R.id.switchAddition);
        switchSubtraction = findViewById(R.id.switchSubtraction);
        switchMultiplication = findViewById(R.id.switchMultiplication);
        switchDivision = findViewById(R.id.switchDivision);
        switchPower = findViewById(R.id.switchPower);
        switchSquareRoot = findViewById(R.id.switchSquareRoot);
        switchCommonFractions = findViewById(R.id.switchCommonFractions);
        switchDecimalFractions = findViewById(R.id.switchDecimalFractions);

        setupSpinners();
        loadSettingsToUI();
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_levels, android.R.layout.simple_spinner_item);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(difficultyAdapter);

        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this,
                R.array.time_options, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(timeAdapter);
    }

    private void loadSettingsToUI() {
        if (currentSettings.getDifficultyLevel() != null) {
            spinnerDifficulty.setSelection(currentSettings.getDifficultyLevel().ordinal());
        }

        long timeMillis = currentSettings.getRoundTimeMillis();
        if (timeMillis == 60000L) spinnerTime.setSelection(0);
        else if (timeMillis == 120000L) spinnerTime.setSelection(1);
        else if (timeMillis == 180000L) spinnerTime.setSelection(2);
        else if (timeMillis == 240000L) spinnerTime.setSelection(3);
        else if (timeMillis == 300000L) spinnerTime.setSelection(4);
        else if (timeMillis == 0L) spinnerTime.setSelection(5);
        else spinnerTime.setSelection(0);

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
        currentSettings.setDifficultyLevel(Settings.Difficulty.values()[spinnerDifficulty.getSelectedItemPosition()]);

        int timePosition = spinnerTime.getSelectedItemPosition();
        if (timePosition == 5) currentSettings.setRoundTimeMillis(0);
        else currentSettings.setRoundTimeMillis((long)(timePosition + 1) * 60000L);

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
