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
import java.math.RoundingMode;


public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewProblem, textViewInput, textViewHint;
    private ProgressBar progressBarTime;
    private ImageButton buttonBackToMenu;

    private Settings gameSettings;
    private ProblemGenerator problemGenerator;
    private Problem currentProblem;
    private SoundPlayer soundPlayer;
    private StorageHelper storageHelper;

    private CountDownTimer roundTimer;
    private long timeRemainingMillis;
    private boolean isEndlessMode;
    private boolean hintUsedForCurrentProblem = false;
    private boolean problemActive = true; // To prevent multiple checks on same problem

    private int correctAnswers = 0;
    private int incorrectAnswers = 0;
    private StringBuilder currentInput = new StringBuilder();

    private static final long DELAY_NEXT_PROBLEM = 750; // ms delay after correct/incorrect
    private static final String TAG = "GameActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        storageHelper = new StorageHelper(this);
        gameSettings = storageHelper.loadSettings();

        if (gameSettings == null || !gameSettings.hasAtLeastOneOperationSelected()) {
            Toast.makeText(this, "No operations selected. Please configure settings first.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "GameActivity started without selected operations. Finishing.");
            finish(); // Close game activity
            return;
        }

        problemGenerator = new ProblemGenerator(gameSettings);
        soundPlayer = new SoundPlayer(this);


        textViewProblem = findViewById(R.id.textViewProblem);
        textViewInput = findViewById(R.id.textViewInput);
        textViewHint = findViewById(R.id.textViewHint);
        progressBarTime = findViewById(R.id.progressBarTime);
        buttonBackToMenu = findViewById(R.id.buttonBackToMenu);

        setupKeypad();
        buttonBackToMenu.setOnClickListener(v -> finishGame(false, false)); // User manually exits

        startGame();
    }

    private void setupKeypad() {
        findViewById(R.id.button0).setOnClickListener(this);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        findViewById(R.id.button5).setOnClickListener(this);
        findViewById(R.id.button6).setOnClickListener(this);
        findViewById(R.id.button7).setOnClickListener(this);
        findViewById(R.id.button8).setOnClickListener(this);
        findViewById(R.id.button9).setOnClickListener(this);
        findViewById(R.id.buttonOK).setOnClickListener(this);
        findViewById(R.id.buttonHint).setOnClickListener(this);
        findViewById(R.id.buttonClear).setOnClickListener(this);
        findViewById(R.id.buttonSlash).setOnClickListener(this);
        findViewById(R.id.buttonDot).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!problemActive && v.getId() != R.id.buttonBackToMenu) return; // Ignore input if problem is being processed

        int id = v.getId();
        if (id == R.id.buttonOK) {
            checkAnswer();
        } else if (id == R.id.buttonHint) {
            showHint();
        } else if (id == R.id.buttonClear) {
            currentInput.setLength(0);
            updateInputDisplay();
        } else if (v instanceof Button) { // Number, dot, or slash
            String text = ((Button) v).getText().toString();

            // Input validation
            if (text.equals(".")) {
                if (currentInput.toString().contains(".") || currentInput.toString().contains("/")) return; // No dot if slash exists or dot exists
            }
            if (text.equals("/")) {
                if (currentInput.toString().contains("/") || currentInput.toString().contains(".")) return; // No slash if dot exists or slash exists
                if (currentInput.length() == 0 || !Character.isDigit(currentInput.charAt(currentInput.length()-1)) ) return; // Must have number before slash
            }
            if (currentInput.length() > 0 && currentInput.toString().endsWith("/") && !Character.isDigit(text.charAt(0))) {
                return; // Must be number after slash
            }


            if (currentInput.length() < 15) { // Limit input length
                currentInput.append(text);
                updateInputDisplay();
            }
        }
    }

    private void startGame() {
        correctAnswers = 0;
        incorrectAnswers = 0;
        isEndlessMode = gameSettings.getRoundTimeMillis() == 0;

        if (isEndlessMode) {
            progressBarTime.setVisibility(View.GONE);
        } else {
            progressBarTime.setVisibility(View.VISIBLE);
            progressBarTime.setMax((int) (gameSettings.getRoundTimeMillis() / 1000));
            startTimer(gameSettings.getRoundTimeMillis());
        }
        generateNewProblem();
    }

    private void generateNewProblem() {
        currentProblem = problemGenerator.generateProblem();
        if (currentProblem == null) {
            Log.e(TAG, "ProblemGenerator returned null problem. Finishing game.");
            Toast.makeText(this, "Error generating problem. Please check settings.", Toast.LENGTH_LONG).show();
            finishGame(false, true); // isError = true
            return;
        }
        textViewProblem.setText(currentProblem.getDisplayEquation() + " =");
        currentInput.setLength(0);
        updateInputDisplay();
        textViewHint.setVisibility(View.GONE);
        hintUsedForCurrentProblem = false;
        problemActive = true;
    }

    private void updateInputDisplay() {
        textViewInput.setText(currentInput.toString());
    }

    private void startTimer(long totalMillis) {
        timeRemainingMillis = totalMillis;
        progressBarTime.setProgress((int)(totalMillis/1000));
        roundTimer = new CountDownTimer(totalMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemainingMillis = millisUntilFinished;
                progressBarTime.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                timeRemainingMillis = 0;
                progressBarTime.setProgress(0);
                finishGame(true, false); // Time's up, not an error
            }
        }.start();
    }

    private void checkAnswer() {
        if (currentProblem == null || currentInput.length() == 0 || !problemActive) return;

        problemActive = false; // Prevent multiple checks
        String userAnswerStr = currentInput.toString();
        boolean isCorrect = false;

        try {
            if (currentProblem.isFractionProblem()) {
                Fraction userAnswerFraction = Fraction.parseFraction(userAnswerStr);
                isCorrect = userAnswerFraction.equals(currentProblem.getCorrectAnswerFraction());
            } else { // Integer or decimal problem
                // "If correct answer ends with “0”,game must accept it without entering zero"
                // This implies comparing numerical value for decimals.
                // For integers, it's exact.
                String correctAnswerCleaned = currentProblem.getCorrectAnswerString();
                BigDecimal correctAnswerBd = new BigDecimal(correctAnswerCleaned);
                BigDecimal userAnswerBd = new BigDecimal(userAnswerStr);

                // Compare numerically. For integers, scale is 0.
                // For decimals, this handles cases like 5.50 vs 5.5
                isCorrect = correctAnswerBd.compareTo(userAnswerBd) == 0;

                // Additional check: if integer answer, user shouldn't input a "real" decimal unless it's like X.0
                if (!correctAnswerCleaned.contains(".") && userAnswerStr.contains(".")) {
                    if (userAnswerBd.stripTrailingZeros().scale() > 0) {
                        // User entered something like "5.2" for an answer of "5".
                        // isCorrect = false; // This is now handled by compareTo if types are different
                    }
                }
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "NumberFormatException for user input: " + userAnswerStr, e);
            isCorrect = false; // Invalid input format
        } catch (ArithmeticException e) { // e.g., division by zero in fraction parsing
            Log.w(TAG, "ArithmeticException for user input: " + userAnswerStr, e);
            isCorrect = false;
        }


        if (isCorrect && !hintUsedForCurrentProblem) {
            correctAnswers++;
            soundPlayer.playCorrectSound();
        } else {
            // If hint was used, it's an incorrect attempt for scoring.
            incorrectAnswers++;
            soundPlayer.playIncorrectSound();
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::generateNewProblem, DELAY_NEXT_PROBLEM);
    }

    private void showHint() {
        if (currentProblem != null && problemActive) {
            textViewHint.setText(currentProblem.getCorrectAnswerString());
            textViewHint.setVisibility(View.VISIBLE);
            hintUsedForCurrentProblem = true;
            // The hint itself doesn't give an incorrect mark immediately,
            // but checkAnswer will count it as incorrect for scoring.
        }
    }

    private void finishGame(boolean timedOut, boolean isError) {
        problemActive = false; // Stop any further game interactions
        if (roundTimer != null) {
            roundTimer.cancel();
        }

        // Only save results if it wasn't an error start
        if (!isError) {
            GameResult result = new GameResult(System.currentTimeMillis(), correctAnswers, incorrectAnswers);
            storageHelper.saveGameResult(result);
            if (timedOut) {
                Toast.makeText(this, R.string.game_over, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Game ended due to an error.", Toast.LENGTH_LONG).show();
        }

        // Delay finish to allow Toast to be seen
        new Handler(Looper.getMainLooper()).postDelayed(super::finish, isError ? 2000 : (timedOut ? 1000: 200));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (roundTimer != null) {
            roundTimer.cancel();
        }
        if (soundPlayer != null) {
            soundPlayer.release();
        }
        Log.d(TAG, "GameActivity onDestroy called.");
    }

    @Override
    public void onBackPressed() {
        if (problemActive || isEndlessMode) { // Allow back press if game is active or endless
            finishGame(false, false); // Game ended by user pressing back, not timed out, not an error
        } else {
            // If problem is not active (e.g. between problems), let system handle back or super.onBackPressed()
            super.onBackPressed();
        }
    }
}
