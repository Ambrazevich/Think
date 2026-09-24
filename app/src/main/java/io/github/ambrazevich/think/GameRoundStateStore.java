package io.github.ambrazevich.think;

import android.os.Bundle;

final class GameRoundStateStore {
    private static Bundle activeRound;

    private GameRoundStateStore() {
    }

    static synchronized void save(Bundle state) {
        activeRound = new Bundle(state);
    }

    static synchronized Bundle get() {
        return activeRound == null ? null : new Bundle(activeRound);
    }

    static synchronized void clear() {
        activeRound = null;
    }
}
