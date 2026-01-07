package com.gerkovger.mep;

import com.gerkovger.mep.logging.Colors;
import com.gerkovger.mep.logging.MepLogger;
import com.gerkovger.mep.persistence.MetaDataRepository;
import com.gerkovger.mep.player.Player;
import com.gerkovger.mep.player.SourceProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Launcher {

    private static final String version = "v-3.0.3";

    static void main(String[] args) {
        System.out.println(
                Colors.BRIGHT_YELLOW + Colors.ITALIC +
                        "Mplayer with Enhanced Persistence " + Colors.RESET +
                        Colors.RED + version + Colors.RESET);

        var remainingArgs = parseCmd(args);

        var repo = new MetaDataRepository();
        var sourceProvider = !remainingArgs.isEmpty() ?
                new SourceProvider(Path.of(remainingArgs.getFirst()), repo) :
                new SourceProvider(repo);
        var player = new Player(sourceProvider);
        player.start();
        player.loadNext();
    }

    private static List<String> parseCmd(String[] args) {
        List<String> remainingArgs = new ArrayList<>(args.length);
        int i = 0;
        while (i < args.length) {
            var arg = args[i];
            if (arg.equals("--loglevel") || arg.equals("-l")) {
                i++;
                if (i < args.length) {
                    MepLogger.INSTANCE.setLogLevel(args[i]);
                }
                break;
            } else if (arg.equals("forget")) {
                forget(args, i);
            } else {
                remainingArgs.add(arg);
            }
            i++;

        }
        return remainingArgs;
    }

    private static void forget(String[] args, int index) {
        List<String> paths = new ArrayList<>();
        for (int i = index + 1; i < args.length && !args[i].startsWith("-"); i++) {
            paths.add(toAbsolutePath(args[i]));
        }
        if (paths.isEmpty()) {
            MetaDataRepository.delete(getAllMediaFileInDir(Path.of("")));
        } else {
            MetaDataRepository.delete(paths);
        }
        System.exit(5);
    }

    private static void play(Path path) {
        var player = new Player(new SourceProvider(new MetaDataRepository()));
        player.start();
        player.loadNext();
    }

    private static List<String> getAllMediaFileInDir(Path dir) {
        try (var stream = Files.list(dir).filter(SourceProvider::isMediaType)) {
            return stream
                    .map(p -> p.toAbsolutePath().toString())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toAbsolutePath(String path) {
        var p = Path.of(path);
        if (Files.exists(p)) return p.toAbsolutePath().toString();
        return Path.of(
                Path.of("").toAbsolutePath().toString(),
                path)
                .toAbsolutePath().toString();
    }

}
