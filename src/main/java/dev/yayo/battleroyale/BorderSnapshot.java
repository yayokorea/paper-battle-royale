package dev.yayo.battleroyale;

import org.bukkit.Location;
import org.bukkit.WorldBorder;

public record BorderSnapshot(Location center, double size, double damageAmount, double damageBuffer,
                             int warningDistance, int warningTimeTicks) {
    static BorderSnapshot capture(WorldBorder b) {
        return new BorderSnapshot(b.getCenter(), b.getSize(), b.getDamageAmount(), b.getDamageBuffer(),
            b.getWarningDistance(), b.getWarningTimeTicks());
    }
    void restore(WorldBorder b) {
        b.setCenter(center); b.setSize(size); b.setDamageAmount(damageAmount); b.setDamageBuffer(damageBuffer);
        b.setWarningDistance(warningDistance); b.setWarningTimeTicks(warningTimeTicks);
    }
}
