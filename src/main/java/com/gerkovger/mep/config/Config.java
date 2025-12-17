package com.gerkovger.mep.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public enum Config {

    INSTANCE;

    private final List<String> mplayerPropertyList;

    private final int endThresholdSeconds;

    private final int continueStepBackSeconds ;

    private final Set<String> mediaTypes;

    private final boolean compactFileNames;

    private final boolean detailedLogs;

    private final String logLevel;

    private final boolean showMPlayerOut;

    private final Path configPath = Path.of(System.getProperty("user.home"), ".config", "mep");

    private final Path metaDataStorePath = Path.of(configPath.toString(), "metadata");

    Config() {
        ConfigLoader cLoader = new ConfigLoader();
        mplayerPropertyList = cLoader.getStringList("property.list", defaultMplayerPropertyList);
        addIfNotExists(mplayerPropertyList, "time_pos", "length", "path");

        mediaTypes = cLoader.getStringSet("media.types", defaultMediaTypes);

        endThresholdSeconds = cLoader.getInt("end.threshold.sec", 1);
        continueStepBackSeconds = cLoader.getInt("cont.stepback.sec", 3);

        compactFileNames = cLoader.getBoolean("compact.filenames", true);

        detailedLogs = cLoader.getBoolean("detailed.logs", false);

        logLevel = cLoader.getString("log.level", "info");
        showMPlayerOut = cLoader.getBoolean("show.mplayer.out", false);
    }
    
    public List<String> getMplayerPropertyList() {
        return mplayerPropertyList;
    }

    public Path getMetaDataStorePath() {
        return metaDataStorePath;
    }

    public int getEndThresholdSeconds() {
        return endThresholdSeconds;
    }

    public int getContinueStepBackSeconds() {
        return continueStepBackSeconds;
    }

    public Set<String> getMediaTypes() {
        return mediaTypes;
    }

    public boolean isCompactFileNames() {
        return compactFileNames;
    }

    public boolean isDetailedLogs() {
        return detailedLogs;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public boolean isShowMPlayerOut() {
        return showMPlayerOut;
    }

    private static void addIfNotExists(List<String> list, String... elements) {
        for (String element : elements) {
            if (!list.contains(element)) list.add(element);
        }
    }

    private final List<String> defaultMplayerPropertyList = List.of(
            "time_pos",
            "length",
            "path",
            "sub",
            "switch_audio",
            "sub_visibility",
            "audio_delay");

    private final Set<String> defaultMediaTypes = Set.of(
            "webm", "mkv", "vob", "ogv", "ogg", "rrc", "gifv", "mng", "mov", "avi",
            "qt", "wmv", "yuv", "rm", "asf", "amv", "mp4", "m4p", "mpg", "mp2", "mpeg",
            "mpe", "mpv", "m4v", "svi", "3gp", "3g2", "mxf", "roq", "nsv", "flv", "f4v", "f4p",
            "f4a", "f4b", "mod");

}
