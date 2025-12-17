package com.gerkovger.mep.player;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Writer implements Runnable {

    private boolean paused = false;

    private MediaState mediaState;

    private final Process process;

    public Writer(Process process) {
        this.process = process;
    }

    @Override
    public void run() {
//        boolean playing = false;
        try (BufferedWriter writer =
                     new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            while (process.isAlive()) {
                String command = Messenger.takeOut();
                if (command.equals(Messenger.MSG_STOP)) {
                    break;
                } else if (command.equals(Messenger.MSG_STARTING_PLAYBACK)) {
                    mediaState = MediaState.PLAYING;
                } else if (command.equals(Messenger.MSG_PAUSE)) {
                    synchronized (this) {
                        if (mediaState == MediaState.PLAYING) mediaState = MediaState.PAUSED;
                        else if (mediaState == MediaState.PAUSED) mediaState = MediaState.PLAYING;
                    }
                } else if (command.equals(Messenger.MSG_END)) {
                    mediaState = MediaState.ENDED;
                }

                if (command.startsWith("loadfile")) write(writer, command);
                else if (mediaState == MediaState.PLAYING) write(writer, command);

            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private static void write(BufferedWriter writer, String command) throws IOException {
        writer.write(command);
        writer.write("\n");
        writer.flush();
    }

    synchronized void togglePaused() {
        paused = !paused;
    }

}
