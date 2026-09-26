package io.github.ambrazevich.think;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import io.github.ambrazevich.think.data.GameResult;
import io.github.ambrazevich.think.data.Problem;
import io.github.ambrazevich.think.data.Settings;
import io.github.ambrazevich.think.gameutils.Fraction;
import io.github.ambrazevich.think.gameutils.EdgeToEdgeInsets;
import io.github.ambrazevich.think.gameutils.InAppReviewPromptPolicy;
import io.github.ambrazevich.think.gameutils.InAppReviewPromptStore;
import io.github.ambrazevich.think.gameutils.ProblemGenerator;
import io.github.ambrazevich.think.gameutils.StorageHelper;
import java.math.BigDecimal;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    static final String EXTRA_MEANINGFUL_ROUND_COMPLETED =
            "io.github.ambrazevich.think.MEANINGFUL_ROUND_COMPLETED";

    private TextView textViewProblem, textViewInput, textViewHint;
    private ProgressBar progressBarTime;
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
    private Handler mainHandler;
    private Runnable hintRunnable;
    private Runnable nextProblemRunnable;
    private Runnable finishRunnable;
    private boolean gameFinished;
    private boolean meaningfulRoundCompleted;
    private long roundEndElapsedRealtime;
    private static final String STATE_PROBLEM = "problem";
    private static final String STATE_INPUT = "input";
    private static final String STATE_CORRECT = "correct";
    private static final String STATE_INCORRECT = "incorrect";
    private static final String STATE_HINT_USED = "hint_used";
    private static final String STATE_HINT_VISIBLE = "hint_visible";
    private static final String STATE_PROBLEM_ACTIVE = "problem_active";
    private static final String STATE_GAME_FINISHED = "game_finished";
    private static final String STATE_MEANINGFUL_ROUND_COMPLETED =
            "meaningful_round_completed";
    private static final String STATE_ROUND_END = "round_end";
    private static final int HINT_VISIBILITY_DURATION_MS = 3000;
    private static final long DELAY_NEXT_PROBLEM = 750;
    private static final String TAG = "GameActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storageHelper = new StorageHelper(this);
        gameSettings = storageHelper.loadSettings();
        mainHandler = new Handler(Looper.getMainLooper());
        if (gameSettings == null || !gameSettings.hasAtLeastOneOperationSelected()) {
            Toast.makeText(this, R.string.configure_settings_first, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        problemGenerator = new ProblemGenerator(gameSettings);
        soundPlayer = new SoundPlayer(this);
        setContentView(R.layout.activity_game);
        EdgeToEdgeInsets.apply(this, findViewById(R.id.gameRoot));
        textViewProblem = findViewById(R.id.textViewProblem);
        textViewInput = findViewById(R.id.textViewInput);
        textViewHint = findViewById(R.id.textViewHint);
        progressBarTime = findViewById(R.id.progressBarTime);
        buttonDot = findViewById(R.id.buttonDot);
        buttonSlash = findViewById(R.id.buttonSlash);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!gameFinished) {
                    finishGame(false, false);
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        setupKeypad();
        Bundle stateToRestore = GameRoundStateStore.get();
        if (stateToRestore == null
                && savedInstanceState != null
                && savedInstanceState.containsKey(STATE_PROBLEM)) {
            stateToRestore = savedInstanceState;
        }
        if (stateToRestore == null) {
            startGame();
        } else {
            restoreGame(stateToRestore);
        }
    }

    private void generateNewProblem() {
        if (gameFinished || isFinishing() || isDestroyed()) return;
        cancelHintHidingTask();
        currentProblem = problemGenerator.generateProblem();
        if (currentProblem == null) {
            Toast.makeText(this, R.string.game_error_gen, Toast.LENGTH_LONG).show();
            finishGame(false, true);
            return;
        }
        updateKeypadForProblem(currentProblem.getAnswerType());
        textViewProblem.setText(getString(R.string.problem_format, currentProblem.getDisplayEquation()));
        currentInput.setLength(0);
        updateInputDisplay();
        textViewHint.setVisibility(View.GONE);
        hintUsedForCurrentProblem = false;
        problemActive = true;
        snapshotRoundState();
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
            soundPlayer.playCorrectSound();
            if (!hintUsedForCurrentProblem) {
                correctAnswers++;
            } else {
                incorrectAnswers++;
            }
            snapshotRoundState();
            nextProblemRunnable = () -> {
                if (!gameFinished && !isFinishing() && !isDestroyed()) {
                    generateNewProblem();
                }
            };
            mainHandler.postDelayed(nextProblemRunnable, DELAY_NEXT_PROBLEM);
        } else {
            soundPlayer.playIncorrectSound();
            incorrectAnswers++;
            currentInput.setLength(0);
            updateInputDisplay();
            problemActive = true;
            snapshotRoundState();
        }
    }

    private void finishGame(boolean timedOut, boolean isError) {
        if (gameFinished) return;
        gameFinished = true;
        GameRoundStateStore.clear();
        problemActive = false;
        cancelPendingCallbacks();
        if (roundTimer != null) { roundTimer.cancel(); roundTimer = null; }
        boolean meaningfulRound = InAppReviewPromptPolicy.isMeaningfulRound(
                correctAnswers, incorrectAnswers, isError);
        if (!isError && storageHelper != null) {
            storageHelper.saveGameResult(new GameResult(System.currentTimeMillis(), correctAnswers, incorrectAnswers));
            if (meaningfulRound) {
                new InAppReviewPromptStore(this).recordCompletedRound(
                        correctAnswers, incorrectAnswers, false);
                meaningfulRoundCompleted = true;
            }
        } else if (isError) { Toast.makeText(this, R.string.game_error_end, Toast.LENGTH_LONG).show(); }
        publishRoundResult();
        finishRunnable = () -> { if (!isFinishing() && !isDestroyed()) GameActivity.super.finish(); };
        mainHandler.postDelayed(finishRunnable, isError ? 2000 : (timedOut ? 1000 : 200));
    }

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
        if (!problemActive) return;
        int id = v.getId();
        if ((id == R.id.buttonDot || id == R.id.buttonSlash) && !v.isEnabled()) return;
        if (id == R.id.buttonOK) { checkAnswer();
        } else if (id == R.id.buttonHint) { showHint();
        } else if (id == R.id.buttonClear) { currentInput.setLength(0); updateInputDisplay(); snapshotRoundState();
        } else if (v instanceof Button) {
            String text = ((Button) v).getText().toString();
            if (text.equals(".") && (currentInput.toString().contains(".") || currentInput.toString().contains("/"))) return;
            if (text.equals("/") && (currentInput.toString().contains("/") || currentInput.toString().contains(".") || currentInput.length() == 0)) return;
            if (currentInput.length() < 15) { currentInput.append(text); updateInputDisplay(); snapshotRoundState(); }
        }
    }
    private void startGame() {
        GameRoundStateStore.clear();
        correctAnswers = 0; incorrectAnswers = 0; gameFinished = false;
        isEndlessMode = gameSettings.getRoundTimeMillis() == 0L;
        if (isEndlessMode) {
            progressBarTime.setVisibility(View.VISIBLE);
            progressBarTime.setMax(100);
            progressBarTime.setProgress(100);
        } else {
            progressBarTime.setVisibility(View.VISIBLE);
            progressBarTime.setMax((int) (gameSettings.getRoundTimeMillis() / 1000));
            roundEndElapsedRealtime = SystemClock.elapsedRealtime() + gameSettings.getRoundTimeMillis();
            startTimer(gameSettings.getRoundTimeMillis());
        }
        generateNewProblem();
    }
    private void restoreGame(Bundle state) {
        if (state.getBoolean(STATE_GAME_FINISHED)) {
            meaningfulRoundCompleted = state.getBoolean(
                    STATE_MEANINGFUL_ROUND_COMPLETED, false);
            publishRoundResult();
            finish(); // The completed result was saved before the Activity was recreated.
            return;
        }
        currentProblem = (Problem) state.getSerializable(STATE_PROBLEM);
        if (currentProblem == null) {
            startGame();
            return;
        }
        correctAnswers = state.getInt(STATE_CORRECT);
        incorrectAnswers = state.getInt(STATE_INCORRECT);
        hintUsedForCurrentProblem = state.getBoolean(STATE_HINT_USED);
        problemActive = state.getBoolean(STATE_PROBLEM_ACTIVE);
        currentInput = new StringBuilder(state.getString(STATE_INPUT, ""));
        isEndlessMode = gameSettings.getRoundTimeMillis() == 0L;
        textViewProblem.setText(getString(R.string.problem_format, currentProblem.getDisplayEquation()));
        updateKeypadForProblem(currentProblem.getAnswerType());
        updateInputDisplay();
        if (state.getBoolean(STATE_HINT_VISIBLE)) {
            showRestoredHint();
        } else {
            textViewHint.setVisibility(View.GONE);
        }
        progressBarTime.setVisibility(View.VISIBLE);
        if (isEndlessMode) {
            progressBarTime.setMax(100);
            progressBarTime.setProgress(100);
        } else {
            roundEndElapsedRealtime = state.getLong(STATE_ROUND_END);
            progressBarTime.setMax((int) (gameSettings.getRoundTimeMillis() / 1000));
            long remaining = roundEndElapsedRealtime - SystemClock.elapsedRealtime();
            if (remaining <= 0) {
                progressBarTime.setProgress(0);
                finishGame(true, false);
                return;
            }
            startTimer(remaining);
        }
        // A correct answer is displayed briefly before the following problem.
        if (!problemActive) {
            nextProblemRunnable = () -> {
                if (!gameFinished && !isFinishing() && !isDestroyed()) generateNewProblem();
            };
            mainHandler.postDelayed(nextProblemRunnable, DELAY_NEXT_PROBLEM);
        }
        snapshotRoundState();
    }
    private void showRestoredHint() {
        textViewHint.setText(currentProblem.getCorrectAnswerString());
        textViewHint.setVisibility(View.VISIBLE);
        hintRunnable = () -> textViewHint.setVisibility(View.GONE);
        mainHandler.postDelayed(hintRunnable, HINT_VISIBILITY_DURATION_MS);
    }
    private void writeRoundState(Bundle outState) {
        outState.putSerializable(STATE_PROBLEM, currentProblem);
        outState.putString(STATE_INPUT, currentInput.toString());
        outState.putInt(STATE_CORRECT, correctAnswers);
        outState.putInt(STATE_INCORRECT, incorrectAnswers);
        outState.putBoolean(STATE_HINT_USED, hintUsedForCurrentProblem);
        outState.putBoolean(STATE_HINT_VISIBLE, textViewHint != null && textViewHint.getVisibility() == View.VISIBLE);
        outState.putBoolean(STATE_PROBLEM_ACTIVE, problemActive);
        outState.putBoolean(STATE_GAME_FINISHED, gameFinished);
        outState.putBoolean(STATE_MEANINGFUL_ROUND_COMPLETED, meaningfulRoundCompleted);
        outState.putLong(STATE_ROUND_END, roundEndElapsedRealtime);
    }
    private void publishRoundResult() {
        Intent result = new Intent().putExtra(
                EXTRA_MEANINGFUL_ROUND_COMPLETED, meaningfulRoundCompleted);
        setResult(meaningfulRoundCompleted ? RESULT_OK : RESULT_CANCELED, result);
    }
    private void snapshotRoundState() {
        if (currentProblem == null || gameFinished) {
            return;
        }
        Bundle state = new Bundle();
        writeRoundState(state);
        GameRoundStateStore.save(state);
    }
    @Override protected void onSaveInstanceState(Bundle outState) {
        writeRoundState(outState);
        GameRoundStateStore.save(outState);
        super.onSaveInstanceState(outState);
    }
    @Override protected void onStop() {
        if (!gameFinished && currentProblem != null) {
            Bundle state = new Bundle();
            writeRoundState(state);
            GameRoundStateStore.save(state);
        }
        super.onStop();
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
            cancelHintHidingTask(); // Cancel any existing hide task
            textViewHint.setText(currentProblem.getCorrectAnswerString()); 
            textViewHint.setVisibility(View.VISIBLE);
            textViewHint.bringToFront(); // Ensure it's on top
            hintUsedForCurrentProblem = true;
            snapshotRoundState();
            hintRunnable = () -> { if (textViewHint != null) textViewHint.setVisibility(View.GONE); };
            mainHandler.postDelayed(hintRunnable, HINT_VISIBILITY_DURATION_MS);
        }
    }
    private void cancelHintHidingTask() {
        if (mainHandler != null && hintRunnable != null) mainHandler.removeCallbacks(hintRunnable);
        hintRunnable = null;
    }
    private void cancelPendingCallbacks() {
        if (mainHandler == null) return;
        if (hintRunnable != null) mainHandler.removeCallbacks(hintRunnable);
        if (nextProblemRunnable != null) mainHandler.removeCallbacks(nextProblemRunnable);
        if (finishRunnable != null) mainHandler.removeCallbacks(finishRunnable);
        hintRunnable = null;
        nextProblemRunnable = null;
        finishRunnable = null;
    }
    @Override protected void onDestroy() {
        cancelPendingCallbacks();
        if (roundTimer != null) roundTimer.cancel();
        if (soundPlayer != null) soundPlayer.release();
        super.onDestroy();
    }
}
