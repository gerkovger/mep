package com.gerkovger.mep.player;

import com.gerkovger.mep.config.Config;
import com.gerkovger.mep.persistence.MetaDataRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SourceProvider {

    private final List<Path> sourceList;

    private final Iterator<Path> iterator;

    private final MetaDataRepository metaDataRepository;

    public SourceProvider(MetaDataRepository repo) {
        this(Path.of("").toAbsolutePath(), repo);
    }

    public SourceProvider(Path source, MetaDataRepository repo) {
        this.metaDataRepository = repo;
        if (Files.isDirectory(source)) {
            sourceList = createList(source);
        } else sourceList = java.util.List.of(source);

        iterator = sourceList.iterator();
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public Path next() {
        return iterator.next();
    }

    public MetaDataRepository getMetaDataRepository() {
        return metaDataRepository;
    }

    public String toString() {
        StringJoiner sj = new StringJoiner("\n");
        for (Path path : sourceList) {
            sj.add("\t" + path.getFileName());
        }
        return "SourceProvider {\n" + sj + "\n}\n";
    }

    private List<Path> createList(Path dir) {
        try (var stream = Files.list(dir).filter(SourceProvider::isMediaType)) {
            List<Path> allList = stream.sorted().toList();
            int last = allList.size() - 1;
            for (; last >= 0 && !metaDataRepository.repoContains(allList.get(last)); last--);
            if (last == -1) return allList;
            List<Path> sourceList = new ArrayList<>(allList.size() - last);
            for (int i = last; i < allList.size(); i++) sourceList.add(allList.get(i));
            return sourceList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Set<String> mediaTypes = Config.INSTANCE.getMediaTypes();
    private static boolean isMediaType(Path p) {
        var fileName = p.toString();
        var i = fileName.lastIndexOf(".");
        return i != -1 && mediaTypes.contains(fileName.substring(i + 1));
    }


}
