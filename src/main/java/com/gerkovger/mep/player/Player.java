package com.gerkovger.mep.player;

import com.gerkovger.mep.config.Config;
import com.gerkovger.mep.logging.MepLogger;
import com.gerkovger.mep.persistence.MetaDataRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Player {

    private static final MepLogger log = MepLogger.INSTANCE;

    private static final Config conf = Config.INSTANCE;

    private static final String[] MPLAYER_CMD =
            new String[] {"gnome-session-inhibit", "--inhibit", "idle",
                    "mplayer", "-slave", "-idle",
                    "-input",
                    "conf=%s.config/mep/input.conf".formatted(System.getProperty("user.home"))};

    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final PropertyCollector propertyCollector = new PropertyCollector();

    private Process process;

    private volatile boolean running = false;

    private Reader reader;

    private final SourceProvider sourceProvider;

    private final MetaDataRepository metaDataRepository;

    public Player(SourceProvider sourceProvider) {
        this.sourceProvider = sourceProvider;
        this.metaDataRepository = sourceProvider.getMetaDataRepository();

        log.info("Creating player with playlist: {}", sourceProvider);
    }

    public void start() {
        try {
            process = new ProcessBuilder(MPLAYER_CMD)
                    .redirectErrorStream(true)
                    .start();

            reader = new Reader(process, sourceProvider);
            executor.execute(reader);
            executor.execute(new Writer(process));
            propertyCollector.start();
            startMPlayerWatcher();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadNext() {
        if (sourceProvider.hasNext()) {
            Path next = sourceProvider.next();
            log.info("loading file {}", next);
            Messenger.sendOut("loadfile \"" + next + "\"");
        } else {
            log.info("THE END of ALL. Goodbye.");
            stop();
        }
    }

    private void startMPlayerWatcher() {
        new Thread(() -> {
            try {
                int exitCode = process.waitFor();  // waits until mplayer quits
                System.out.println("MPlayer exited with code " + exitCode);
            } catch (InterruptedException ignored) {
            } finally {
                stop();
            }
        }, "MPlayer-Watcher").start();
    }

    private void stop() {
        running = false;
        Messenger.sendOut(Messenger.MSG_STOP);
        if (process != null && process.isAlive()) {
            process.destroy();
        }

        executor.shutdownNow();
        propertyCollector.shutdown();

        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ignored) {
        }

        System.out.println("Player stopped cleanly.");

        metaDataRepository.saveOrUpdateRepository(reader.getMetaData());
    }


}
