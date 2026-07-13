package dev.yayo.battleroyale;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class BattleRoyalePlugin extends JavaPlugin {
    private GameManager game;
    @Override public void onEnable() {
        saveDefaultConfig();
        try { game = new GameManager(this, GameConfig.load(getConfig())); }
        catch (RuntimeException e) { getLogger().severe(e.getMessage()); getServer().getPluginManager().disablePlugin(this); return; }
        PluginCommand command = getCommand("br");
        if (command == null) throw new IllegalStateException("plugin.yml에 br 명령이 없습니다.");
        BattleRoyaleCommand handler = new BattleRoyaleCommand(game);
        command.setExecutor(handler); command.setTabCompleter(handler);
        getServer().getPluginManager().registerEvents(new GameListener(game), this);
    }
    @Override public void onDisable() { if (game != null) game.shutdown(); }
}
