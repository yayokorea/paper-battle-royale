package dev.yayo.battleroyale;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class BattleRoyalePlugin extends JavaPlugin {
    private GameManager game;
    private LobbyManager lobby;
    @Override public void onEnable() {
        saveDefaultConfig();
        try { lobby=new LobbyManager(this); game = new GameManager(this, GameConfig.load(getConfig()), lobby); }
        catch (RuntimeException e) { getLogger().severe(e.getMessage()); getServer().getPluginManager().disablePlugin(this); return; }
        PluginCommand command = getCommand("br");
        if (command == null) throw new IllegalStateException("plugin.yml에 br 명령이 없습니다.");
        BattleRoyaleCommand handler = new BattleRoyaleCommand(game);
        command.setExecutor(handler); command.setTabCompleter(handler);
        getServer().getPluginManager().registerEvents(new GameListener(game), this);
        getServer().getPluginManager().registerEvents(new LobbyListener(lobby, game), this);
        lobby.initialize().whenComplete((unused,error)->{
            if(error!=null){getLogger().severe("로비 생성 실패: "+error.getMessage());getServer().getPluginManager().disablePlugin(this);}
            else getServer().getOnlinePlayers().forEach(p->{if(game.phase()==GamePhase.IDLE)lobby.prepareWaitingPlayer(p);});
        });
    }
    @Override public void onDisable() { if (game != null) game.shutdown(); }
}
