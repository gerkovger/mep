package com.gerkovger.mep.persistence;

import com.gerkovger.mep.config.Config;
import com.gerkovger.mep.logging.MepLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;

public class MetaDataRepository {

    private static final MepLogger log = MepLogger.INSTANCE;

    public MetaDataRepository() {

    }

    public MetaData get(String path) {
        log.info("Finding metadata for path '{}'", Path.of(path));
        if (!Files.exists(Config.INSTANCE.getMetaDataStorePath())) return new MetaData();
        try (var lines = Files.lines(Config.INSTANCE.getMetaDataStorePath())) {
            String kv = lines.filter(line -> lineHasPath(line, path))
                    .map(line -> line.substring(line.lastIndexOf(";") + 1))
                    .findFirst()
                    .orElse("");
            log.info("Loading persisted properties...");
            log.debug("Creating MetaData from string '{}'", kv);
            return create(kv);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean repoContains(Path path) {
        try (var lines = Files.lines(Config.INSTANCE.getMetaDataStorePath())) {
            return lines.anyMatch(line -> lineHasPath(line, path.toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean lineHasPath(String line, String path) {
        var pt = line.split(";");
        return pt.length == 3 && Objects.equals(pt[0], path);
    }

    public void saveOrUpdateRepository(MetaData metaData) {
        var repositoryPath = Config.INSTANCE.getMetaDataStorePath();
        StringBuilder sb = new StringBuilder(metaData.serialize());
        if (Files.exists(repositoryPath)) {
            try (var lines = Files.lines(repositoryPath)) {
                lines
                        .filter(line -> !lineHasPath(line, metaData.get("path")))
                        .forEach(line -> sb.append("\n").append(line));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeRepo(sb.toString());
    }

    private void writeRepo(String repoLines) {
        try {
            Files.writeString(
                    Config.INSTANCE.getMetaDataStorePath(),
                    repoLines, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MetaData create(String serialized) {
        var md = new MetaData();
        if (serialized.isBlank()) return md;
        Arrays.stream(serialized.split(","))
                .map(elem -> elem.split("="))
                .forEach(arr -> md.put(arr[0], arr[1]));
        return md;
    }

}
