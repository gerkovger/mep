package com.gerkovger.mep;

import com.gerkovger.mep.logging.Colors;
import com.gerkovger.mep.persistence.MetaDataRepository;
import com.gerkovger.mep.player.Player;
import com.gerkovger.mep.player.SourceProvider;

import java.nio.file.Path;

public class Launcher {

    private static final String version = "v-3.0.3";

    static void main(String[] args) {
        System.out.println(
                Colors.BRIGHT_YELLOW + Colors.ITALIC +
                        "Mplayer with Enhanced Persistence " + Colors.RESET +
                        Colors.RED + version + Colors.RESET);

        var repo = new MetaDataRepository();
        var sourceProvider = args.length > 0 ?
                new SourceProvider(Path.of(args[0]), repo) :
                new SourceProvider(repo);
        var player = new Player(sourceProvider);
        player.start();
        player.loadNext();
    }

    private static void play(Path path) {
        var player = new Player(new SourceProvider(new MetaDataRepository()));
        player.start();
        player.loadNext();
    }

}
