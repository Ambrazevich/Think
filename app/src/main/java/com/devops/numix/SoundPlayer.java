package com.devops.numix;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class SoundPlayer {
    private MediaPlayer correctSoundPlayer;
    private MediaPlayer incorrectSoundPlayer;
    private Context context;

    public SoundPlayer(Context context) {        this.context = context.getApplicationContext();
        try {
            correctSoundPlayer = MediaPlayer.create(this.context, R.raw.correct);
            incorrectSoundPlayer = MediaPlayer.create(this.context, R.raw.incorrect);

            if (correctSoundPlayer == null) {
                Log.e("SoundPlayer", "Failed to create MediaPlayer for correct.mp3. Check res/raw.");
            }
            if (incorrectSoundPlayer == null) {
                Log.e("SoundPlayer", "Failed to create MediaPlayer for incorrect.mp3. Check res/raw.");
            }

        } catch (Exception e) {
            Log.e("SoundPlayer", "Error initializing MediaPlayers. Ensure correct.mp3 and incorrect.mp3 are in res/raw.", e);
            correctSoundPlayer = null;
            incorrectSoundPlayer = null;
        }
    }

    public void playCorrectSound() {
        if (correctSoundPlayer != null) {
            try {
                if (correctSoundPlayer.isPlaying()) {
                    correctSoundPlayer.stop();
                    correctSoundPlayer.prepare(); // Prepare again after stopping
                }
                correctSoundPlayer.start();
            } catch (IllegalStateException e) {
                Log.e("SoundPlayer", "IllegalStateException for correct sound. Re-initializing.", e);
                try { correctSoundPlayer.release(); } catch (Exception ignored) {}
                correctSoundPlayer = MediaPlayer.create(context, R.raw.correct);
                if (correctSoundPlayer != null) correctSoundPlayer.start();
            }
            catch (Exception e) { // Catch other exceptions like IOException during prepare
                Log.e("SoundPlayer", "Error playing correct sound", e);
            }
        } else {
            Log.w("SoundPlayer", "Correct sound MediaPlayer not initialized or file missing.");
        }
    }

    public void playIncorrectSound() {
        if (incorrectSoundPlayer != null) {
            try {
                if (incorrectSoundPlayer.isPlaying()) {
                    incorrectSoundPlayer.stop();
                    incorrectSoundPlayer.prepare(); // Prepare again after stopping
                }
                incorrectSoundPlayer.start();
            } catch (IllegalStateException e) {
                Log.e("SoundPlayer", "IllegalStateException for incorrect sound. Re-initializing.", e);
                try { incorrectSoundPlayer.release(); } catch (Exception ignored) {}
                incorrectSoundPlayer = MediaPlayer.create(context, R.raw.incorrect);
                if (incorrectSoundPlayer != null) incorrectSoundPlayer.start();
            } catch (Exception e) {
                Log.e("SoundPlayer", "Error playing incorrect sound", e);
            }
        } else {
            Log.w("SoundPlayer", "Incorrect sound MediaPlayer not initialized or file missing.");
        }
    }

    public void release() {
        try {
            if (correctSoundPlayer != null) {
                if (correctSoundPlayer.isPlaying()) {
                    correctSoundPlayer.stop();
                }
                correctSoundPlayer.release();
                correctSoundPlayer = null;
            }
            if (incorrectSoundPlayer != null) {
                if (incorrectSoundPlayer.isPlaying()) {
                    incorrectSoundPlayer.stop();
                }
                incorrectSoundPlayer.release();
                incorrectSoundPlayer = null;
            }
        } catch (Exception e) {
            Log.e("SoundPlayer", "Error releasing MediaPlayers", e);
        }
    }
}
