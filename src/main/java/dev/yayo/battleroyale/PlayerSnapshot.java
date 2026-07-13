package dev.yayo.battleroyale;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public record PlayerSnapshot(Location location, GameMode gameMode, ItemStack[] contents, ItemStack[] armor,
    ItemStack offhand, double health, int food, float saturation, int level, float exp,
    boolean allowFlight, boolean flying) {
    static PlayerSnapshot capture(Player p) {
        return new PlayerSnapshot(p.getLocation().clone(), p.getGameMode(), p.getInventory().getContents().clone(),
            p.getInventory().getArmorContents().clone(), p.getInventory().getItemInOffHand().clone(), p.getHealth(),
            p.getFoodLevel(), p.getSaturation(), p.getLevel(), p.getExp(), p.getAllowFlight(), p.isFlying());
    }
    void restore(Player p) {
        p.getInventory().setContents(contents); p.getInventory().setArmorContents(armor);
        p.getInventory().setItemInOffHand(offhand); p.setGameMode(gameMode);
        double max = p.getAttribute(Attribute.MAX_HEALTH).getValue(); p.setHealth(Math.min(health, max));
        p.setFoodLevel(food); p.setSaturation(saturation); p.setLevel(level); p.setExp(exp);
        p.setAllowFlight(allowFlight); p.setFlying(flying && allowFlight); p.teleportAsync(location);
    }
}
