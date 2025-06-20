package com.devops.numix;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.devops.numix.data.GameResult;
import com.devops.numix.data.Problem;
import com.devops.numix.data.Settings;
import com.devops.numix.gameutils.Fraction;
import com.devops.numix.gameutils.ProblemGenerator;
import com.devops.numix.gameutils.StorageHelper;
import java.math.BigDecimal;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewProblem, textViewInput, textViewHint;
    private ProgressBar progressBarTime;
    private ImageButton buttonBackToMenu;
    private Button buttonDot, buttonSlash;
    private Settings gameSettings;
    private ProblemGenerator problemGenerator;
    private Problem currentProblem;
    private SoundPlayer soundPlayer;
    private StorageHelper storageHelper;
    private CountDownTimer roundTimer;
    private boolean isEndlessMode, hintUsedForCurrentProblem, problemActive = true;
    private int correctAnswers = 0, incorrectAnswers = 0;
    private StringBuilder currentInput = new StringBuilder();
    private Handler hintHandler;
    private Runnable hintRunnable;
    private static final int HINT_VISIBILITY_DURATION_MS = 750;
    private static final long DELAY_NEXT_PROBLEM = 750;
    private static final String TAG = "GameActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storageHelper = new StorageHelper(this);
        gameSettings = storageHelper.loadSettings();
        hintHandler = new Handler(Looper.getMainLooper());
        if (gameSettings == null || !gameSettings.hasAtLeastOneOperationSelected()) {
            Toast.makeText(this, "No operations selected. Please configure settings first.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        problemGenerator = new ProblemGenerator(gameSettings);
        soundPlayer = new SoundPlayer(this);
        setContentView(R.layout.activity_game);
        textViewProblem = findViewById(R.id.textViewProblem);
        textViewInput = findViewById(R.id.textViewInput);
        textViewHint = findViewById(R.id.textViewHint);
        progressBarTime = findViewById(R.id.progressBarTime);
        buttonBackToMenu = findViewById(R.id.buttonBackToMenu);
        buttonDot = findViewById(R.id.buttonDot);
        buttonSlash = findViewById(R.id.buttonSlash);
        setupKeypad();
        buttonBackToMenu.setOnClickListener(v -> finishGame(false, false));
        startGame();
    }

    private void generateNewProblem() {
        cancelHintHidingTask();
        currentProblem = problemGenerator.generateProblem();
        if (currentProblem == null) {
            Toast.makeText(this, "Error: Could not generate a new problem.", Toast.LENGTH_LONG).show();
            finishGame(false, true);
            return;
        }
        updateKeypadForProblem(currentProblem.getAnswerType());
        textViewProblem.setText(currentProblem.getDisplayEquation() + " =");
        currentInput.setLength(0);
        updateInputDisplay();
        textViewHint.setVisibility(View.GONE);
        hintUsedForCurrentProblem = false;
        problemActive = true;
    }

    private void updateKeypadForProblem(Problem.AnswerType answerType) {
        boolean enableSlash = (answerType == Problem.AnswerType.FRACTION);
        boolean enableDot = (answerType == Problem.AnswerType.DECIMAL);
        if (buttonSlash != null) {
            buttonSlash.setEnabled(enableSlash);
            buttonSlash.setAlpha(enableSlash ? 1.0f : 0.5f);
        }
        if (buttonDot != null) {
            buttonDot.setEnabled(enableDot);
            buttonDot.setAlpha(enableDot ? 1.0f : 0.5f);
        }
    }

    private void checkAnswer() {
        if (currentProblem == null || currentInput.length() == 0 || !problemActive) return;

        problemActive = false; // Disable input while we check
        String userAnswerStr = currentInput.toString();
        boolean isCorrect = false;

        try {
            switch (currentProblem.getAnswerType()) {
                case FRACTION:
                    isCorrect = Fraction.parseFraction(userAnswerStr).equals(currentProblem.getCorrectAnswerFraction());
                    break;
                case DECIMAL: case INTEGER:
                    isCorrect = new BigDecimal(userAnswerStr).compareTo(new BigDecimal(currentProblem.getCorrectAnswerString())) == 0;
                    break;
            }
        } catch (Exception e) {
            Log.w(TAG, "User input could not be parsed or compared: " + userAnswerStr, e);
            isCorrect = false;
        }

        if (isCorrect) {
            // --- CORRECT ANSWER LOGIC ---
            soundPlayer.playCorrectSound();
            if (!hintUsedForCurrentProblem) {
                correctAnswers++;
            } else {
                incorrectAnswers++; // Hinted answer still counts as incorrect for scoring
            }
            // Schedule the next problem to appear
            new Handler(Looper.getMainLooper()).postDelayed(this::generateNewProblem, DELAY_NEXT_PROBLEM);
        } else {
            // --- INCORRECT ANSWER LOGIC ---
            soundPlayer.playIncorrectSound();
            incorrectAnswers++;
            // Clear the user's input so they can try again on the same problem
            currentInput.setLength(0);
            updateInputDisplay();
            // Re-enable the keypad for another attempt
            problemActive = true;
        }
    }

    // --- UPDATED METHOD ---
    private void finishGame(boolean timedOut, boolean isError) {
        problemActive = false; if (roundTimer != null) { roundTimer.cancel(); roundTimer = null; }
        if (!isError && storageHelper != null) {
            storageHelper.saveGameResult(new GameResult(System.currentTimeMillis(), correctAnswers, incorrectAnswers));
            if (timedOut) {
                // --- FIX: The "Round Over!" toast message is now removed ---
                // Toast.makeText(this, R.string.game_over, Toast.LENGTH_SHORT).show();
            }
        } else if (isError) { Toast.makeText(this, "Game ended due to an error.", Toast.LENGTH_LONG).show(); }
        new Handler(Looper.getMainLooper()).postDelayed(() -> { if (!isFinishing()) super.finish(); }, isError ? 2000 : (timedOut ? 1000: 200));
    }

    // --- Other methods (unchanged) ---
    private void setupKeypad() {
        setClickListener(R.id.button0); setClickListener(R.id.button1); setClickListener(R.id.button2);
        setClickListener(R.id.button3); setClickListener(R.id.button4); setClickListener(R.id.button5);
        setClickListener(R.id.button6); setClickListener(R.id.button7); setClickListener(R.id.button8);
        setClickListener(R.id.button9); setClickListener(R.id.buttonOK); setClickListener(R.id.buttonHint);
        setClickListener(R.id.buttonClear); setClickListener(R.id.buttonSlash); setClickListener(R.id.buttonDot);
    }
    private void setClickListener(int id) {
        View v = findViewById(id); if (v != null) v.setOnClickListener(this);
    }
    @Override public void onClick(View v) {
        if (!problemActive && v.getId() != R.id.buttonBackToMenu) return;
        int id = v.getId();
        if ((id == R.id.buttonDot || id == R.id.buttonSlash) && !v.isEnabled()) return;
        if (id == R.id.buttonOK) { checkAnswer();
        } else if (id == R.id.buttonHint) { showHint();
        } else if (id == R.id.buttonClear) { currentInput.setLength(0); updateInputDisplay();
        } else if (v instanceof Button) {
            String text = ((Button) v).getText().toString();
            if (text.equals(".") && (currentInput.toString().contains(".") || currentInput.toString().contains("/"))) return;
            if (text.equals("/") && (currentInput.toString().contains("/") || currentInput.toString().contains(".") || currentInput.length() == 0)) return;
            if (currentInput.length() < 15) { currentInput.append(text); updateInputDisplay(); }
        }
    }
    private void startGame() {
        correctAnswers = 0; incorrectAnswers = 0; isEndlessMode = gameSettings.getRoundTimeMillis() == 0L;
        if (isEndlessMode) { progressBarTime.setVisibility(View.GONE); } else {
            progressBarTime.setVisibility(View.VISIBLE);
            progressBarTime.setMax((int) (gameSettings.getRoundTimeMillis() / 1000));
            startTimer(gameSettings.getRoundTimeMillis());
        }
        generateNewProblem();
    }
    private void updateInputDisplay() { if (textViewInput != null) textViewInput.setText(currentInput.toString()); }
    private void startTimer(long totalMillis) {
        if (progressBarTime != null) progressBarTime.setProgress((int)(totalMillis/1000));
        roundTimer = new CountDownTimer(totalMillis, 1000) {
            @Override public void onTick(long millis) { if (progressBarTime != null) progressBarTime.setProgress((int) (millis / 1000)); }
            @Override public void onFinish() { if (progressBarTime != null) progressBarTime.setProgress(0); finishGame(true, false); }
        }.start();
    }
    private void showHint() {
        if (currentProblem != null && problemActive && textViewHint != null) {
            textViewHint.setText(currentProblem.getCorrectAnswerString()); textViewHint.setVisibility(View.VISIBLE); hintUsedForCurrentProblem = true;
            hintRunnable = () -> { if (textViewHint != null) textViewHint.setVisibility(View.GONE); };
            hintHandler.postDelayed(hintRunnable, HINT_VISIBILITY_DURATION_MS);
        }
    }
    private void cancelHintHidingTask() { if (hintHandler != null && hintRunnable != null) hintHandler.removeCallbacks(hintRunnable); }
    @Override protected void onDestroy() {
        super.onDestroy(); if (roundTimer != null) roundTimer.cancel(); if (soundPlayer != null) soundPlayer.release();
        cancelHintHidingTask();
    }
    @Override public void onBackPressed() {
        if (problemActive || isEndlessMode) { finishGame(false, false); } else { if (!isFinishing()) super.onBackPressed(); }
    }
}
