package dev.yayo.battleroyale;

import org.bukkit.configuration.file.FileConfiguration;

public record GameConfig(int minPlayers, int maxPlayers, int preparationSeconds,
                         int maxMatchSeconds, int teleportRadius, int minimumPlayerDistance,
                         double borderStartSize, double borderEndSize, int borderShrinkDelaySeconds,
                         int borderShrinkDurationSeconds, int randomLocationAttempts) {
    public static GameConfig load(FileConfiguration c) {
        GameConfig g = new GameConfig(c.getInt("min-players"), c.getInt("max-players"),
            c.getInt("preparation-seconds"), c.getInt("max-match-seconds"), c.getInt("teleport-radius"),
            c.getInt("minimum-player-distance"), c.getDouble("border-start-size"),
            c.getDouble("border-end-size"), c.getInt("border-shrink-delay-seconds"),
            c.getInt("border-shrink-duration-seconds"), c.getInt("random-location-attempts"));
        if (g.minPlayers < 2 || g.maxPlayers < g.minPlayers || g.preparationSeconds < 0 ||
            g.maxMatchSeconds <= g.preparationSeconds || g.teleportRadius < 32 ||
            g.minimumPlayerDistance < 0 || g.borderStartSize <= g.borderEndSize || g.borderEndSize < 1 ||
            g.borderShrinkDelaySeconds < g.preparationSeconds || g.borderShrinkDurationSeconds < 1 ||
            g.randomLocationAttempts < 1) throw new IllegalArgumentException("config.yml 값의 범위 또는 상호 관계가 잘못되었습니다.");
        return g;
    }
}
