package dev.yayo.battleroyale;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

final class GameListener implements Listener {
    private final GameManager game; GameListener(GameManager game){this.game=game;}
    @EventHandler(ignoreCancelled=true) public void onDamage(EntityDamageByEntityEvent e){
        if(!(e.getEntity() instanceof Player victim)||!game.isAlive(victim.getUniqueId()))return;
        Player attacker=null; if(e.getDamager() instanceof Player p)attacker=p; else if(e.getDamager() instanceof Projectile pr && pr.getShooter() instanceof Player p)attacker=p;
        if(attacker!=null && game.isAlive(attacker.getUniqueId()) && !game.pvpEnabled())e.setCancelled(true);
    }
    @EventHandler public void onDeath(PlayerDeathEvent e){if(game.isAlive(e.getPlayer().getUniqueId()))game.eliminate(e.getPlayer(),"사망");}
    @EventHandler public void onQuit(PlayerQuitEvent e){game.handleQuit(e.getPlayer());}
    @EventHandler public void onJoin(PlayerJoinEvent e){game.handleJoin(e.getPlayer());}
}
