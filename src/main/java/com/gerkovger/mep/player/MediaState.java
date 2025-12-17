package com.gerkovger.mep.player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public enum MediaState {

    LOADED, STARTED, PLAYING, PAUSED, ENDED;

    static {
        LOADED.setReachableStates(Set.of(STARTED));
        STARTED.setReachableStates(Set.of(PLAYING));
        PLAYING.setReachableStates(Set.of(PAUSED, ENDED));
        PAUSED.setReachableStates(Set.of(PLAYING));
        ENDED.setReachableStates(Set.of(LOADED));
    }

    Set<MediaState> reachableStates;

    private void setReachableStates(Set<MediaState> reachableStates) {
        this.reachableStates = reachableStates;
    }

    public static boolean isReachable(MediaState from, MediaState to) {
        return from.reachableStates.contains(to);
    }

}
