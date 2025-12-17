package com.gerkovger.mep.persistence;

import com.gerkovger.mep.config.Config;
import com.gerkovger.mep.logging.MepLogger;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MetaData extends TreeMap<String, String> {

    private static final MepLogger log = MepLogger.INSTANCE;

    public String serialize() {
        stepBack();
        long epoch = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        var mapStr = entrySet().stream()
                .filter(e -> !e.getKey().equals("path"))
                .filter(e -> !e.getKey().equals("length"))
                .filter(e -> !e.getKey().equals("ERROR"))
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(","));
        return get("path") + ";" + epoch + ";" + mapStr;
    }

    private void stepBack() {
        if (!containsKey("time_pos")) return;
        var sbSec = Config.INSTANCE.getContinueStepBackSeconds();
        var newTimePos = Math.max(Float.parseFloat(get("time_pos")) - sbSec, 0);
        put("time_pos", String.valueOf(newTimePos));
    }

}
