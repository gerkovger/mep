package com.gerkovger.mep.player;

import com.gerkovger.mep.config.Config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PropertyCollector {

    private final ScheduledExecutorService collector = Executors.newSingleThreadScheduledExecutor();

    void start() {
        collector.scheduleAtFixedRate(
                this::collect,
                0,
                1,
                TimeUnit.SECONDS);
    }

    void shutdown() {
        collector.shutdownNow();
    }

    private void collect() {
        for (String property : Config.INSTANCE.getMplayerPropertyList()) {
            Messenger.sendOut("get_property " + property);
        }
    }

}
