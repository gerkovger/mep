package com.gerkovger.mep.player;

import com.gerkovger.mep.config.Config;
import com.gerkovger.mep.logging.Colors;
import com.gerkovger.mep.logging.MepLogger;
import com.gerkovger.mep.persistence.MetaData;
import com.gerkovger.mep.persistence.MetaDataRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

import static com.gerkovger.mep.player.Messenger.*;

public class Reader implements Runnable {

    private static final MepLogger log = MepLogger.INSTANCE;

    private final Process process;

    private final MetaDataRepository repo;

    private final SourceProvider sourceProvider;

    private MetaData metaData;

    public Reader(Process process, SourceProvider sourceProvider) {
        this.process = process;
        this.repo = sourceProvider.getMetaDataRepository();
        this.sourceProvider = sourceProvider;
    }

    @Override
    public void run() {
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("A:") && !line.startsWith("ANS_") && Config.INSTANCE.isShowMPlayerOut())
                    System.out.println("[" + Colors.BRIGHT_YELLOW + Colors.BOLD + "mplayer" + Colors.RESET + "]: " + line);

                var vPos = getVPos(line);
                if (vPos != -1.0) {
                    if (metaData != null && metaData.containsKey("length")) {
                        var l = Float.parseFloat(metaData.get("length"));
                        if (l - vPos < Config.INSTANCE.getEndThresholdSeconds()) {
                            log.info("Reached the end of {}", metaData.get("path"));
                            metaData.put("time_pos", "0");
//                            playNext();
                        }
                    }
                }

                if (line.trim().equals("Starting playback...")) {
                    sendOut(MSG_STARTING_PLAYBACK);
                    if (metaData != null) {
                        metaData.entrySet().stream()
                                .filter(e -> !e.getKey().equals("path"))
                                .forEach(e -> sendOut("set_property " + e.getKey() + " " + e.getValue()));
                    }
                } else if (line.startsWith("Playing ")) {
                    int j = line.trim().endsWith(".") ? line.lastIndexOf(".") : line.length() - 1;
                    var fileName = line.substring(8, j);
                    log.info("Starting playing '{}'", Path.of(fileName));
                    metaData = repo.get(fileName);
                } else if (line.startsWith("ANS_") && metaData != null) {
                    var kv = line.substring(4);
                    var pt = kv.split("=");
                    metaData.put(pt[0], pt[1]);
                } else if (line.equals(MP_MSG_NULL_PATH)) {
                    playNext();
                } else if (line.trim().equals(MP_MSG_PAUSE)) {
                    sendOut(MSG_PAUSE);
                } else if (line.startsWith(MP_MSG_NO_BIND)) {
                    log.info("Key pressed: " + line.substring(MP_MSG_NO_BIND.length() + 1));
                } else if (line.startsWith(MP_MSG_INVALID_KEY)) {
                    var key = line.substring(MP_MSG_INVALID_KEY.length() + 1, line.indexOf(":")).trim();
                    log.info("Key pressed: '{}'", key);
                    if (key.equals("i")) printInfo();
                }


            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printInfo() {
        sendOut("osd_show_text jkldsaéjfkldsaéfjkslaé");
    }

    private void playNext() {
        repo.saveOrUpdateRepository(metaData);
        sendOut(MSG_END);
        if (sourceProvider.hasNext()) {
            var next = sourceProvider.next();
            sendOut("loadfile \"" + next + "\"");
        } else {
            sendOut(MSG_STOP);
        }
    }

    public MetaData getMetaData() {
        return metaData;
    }

    private static float getVPos(String line) {
        line = line.trim();
        int i = 0;
        for (; i < line.length() - 1 && !(line.charAt(i) == 'V' && line.charAt(i + 1) == ':'); i++);
        if (i > line.length() - 2) return -1;
        i += 2;
        for (; i < line.length() && !Character.isDigit(line.charAt(i)); i++);
        StringBuilder sb = new StringBuilder();
        for (; i < line.length() && (Character.isDigit(line.charAt(i)) || line.charAt(i) == '.'); i++) {
            sb.append(line.charAt(i));
        }
        try {
            return Float.parseFloat(sb.toString());
        } catch (NumberFormatException e) {
            throw new RuntimeException(sb.toString());
        }
    }

    static void main() {
        var line = "A:   0.1 V:   0.0 A-V:  0.061 ct:  0.000   0/  0 ??% ??% ??,?% 0 0";
        System.out.println(getVPos(line));
    }

}
