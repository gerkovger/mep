package com.gerkovger.mep.player;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Messenger {

    public static final String MSG_STOP = "__STOP__";

    public static final String MSG_PAUSE = "__PAUSE__";

    public static final String MSG_END = "__END__";

    public static final String MP_MSG_PAUSE = "=====  PAUSE  =====";

    public static final String MP_MSG_NULL_PATH = "ANS_path=(null)";

    public static final String MP_MSG_NO_BIND = "No bind found for key";

    public static final String MP_MSG_INVALID_KEY = "Invalid command for bound key";

    public static final String MSG_STARTING_PLAYBACK = "__STARTING_PLAYBACK__";

    private static BlockingQueue<String> outQ = new LinkedBlockingQueue<>();

    public static void sendOut(String msg) {
        outQ.add(msg);
    }

    public static String takeOut() throws InterruptedException {
        return outQ.take();
    }

}
