package dev.yayo.battleroyale;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

final class LobbyListener implements Listener {
    private final LobbyManager lobby; private final GameManager game;
    LobbyListener(LobbyManager lobby,GameManager game){this.lobby=lobby;this.game=game;}
    @EventHandler public void onJoin(PlayerJoinEvent e){
        if(game.phase()==GamePhase.IDLE)lobby.prepareWaitingPlayer(e.getPlayer()); else if(!game.isParticipant(e.getPlayer().getUniqueId()))lobby.prepareWaitingPlayer(e.getPlayer());
    }
    @EventHandler(ignoreCancelled=true) public void onMove(PlayerMoveEvent e){if(lobby.fellFromLobby(e.getTo()))lobby.teleport(e.getPlayer());}
    @EventHandler(ignoreCancelled=true) public void onBreak(BlockBreakEvent e){if(lobby.inLobby(e.getBlock().getLocation()))e.setCancelled(true);}
    @EventHandler(ignoreCancelled=true) public void onPlace(BlockPlaceEvent e){if(lobby.inLobby(e.getBlock().getLocation()))e.setCancelled(true);}
    @EventHandler(ignoreCancelled=true) public void onBurn(BlockBurnEvent e){if(lobby.inLobby(e.getBlock().getLocation()))e.setCancelled(true);}
    @EventHandler(ignoreCancelled=true) public void onIgnite(BlockIgniteEvent e){if(lobby.inLobby(e.getBlock().getLocation()))e.setCancelled(true);}
    @EventHandler(ignoreCancelled=true) public void onFlow(BlockFromToEvent e){if(lobby.inLobby(e.getToBlock().getLocation()))e.setCancelled(true);}
    @EventHandler(ignoreCancelled=true) public void onBucket(PlayerBucketEmptyEvent e){if(lobby.inLobby(e.getBlockClicked().getLocation()))e.setCancelled(true);}
    @EventHandler(ignoreCancelled=true) public void onBucket(PlayerBucketFillEvent e){if(lobby.inLobby(e.getBlockClicked().getLocation()))e.setCancelled(true);}
    @EventHandler(ignoreCancelled=true) public void onChange(EntityChangeBlockEvent e){if(lobby.inLobby(e.getBlock().getLocation()))e.setCancelled(true);}
    @EventHandler(ignoreCancelled=true) public void onExplosion(EntityExplodeEvent e){e.blockList().removeIf(b->lobby.inLobby(b.getLocation()));}
    @EventHandler(ignoreCancelled=true) public void onBlockExplosion(BlockExplodeEvent e){e.blockList().removeIf(b->lobby.inLobby(b.getLocation()));}
    @EventHandler(ignoreCancelled=true) public void onDamage(EntityDamageEvent e){if(e.getEntity() instanceof Player p&&lobby.inLobby(p.getLocation()))e.setCancelled(true);}
    @EventHandler(ignoreCancelled=true) public void onHunger(FoodLevelChangeEvent e){if(e.getEntity() instanceof Player p&&lobby.inLobby(p.getLocation())){e.setCancelled(true);p.setFoodLevel(20);}}
    @EventHandler public void onRespawn(PlayerRespawnEvent e){if(game.isParticipant(e.getPlayer().getUniqueId())&&!game.isAlive(e.getPlayer().getUniqueId())){e.setRespawnLocation(lobby.spawn());e.getPlayer().setGameMode(GameMode.SPECTATOR);}}
}
