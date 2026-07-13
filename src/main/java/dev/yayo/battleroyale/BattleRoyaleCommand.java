package dev.yayo.battleroyale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

final class BattleRoyaleCommand implements TabExecutor {
    private final GameManager game;
    BattleRoyaleCommand(GameManager game){this.game=game;}
    @Override public boolean onCommand(@NotNull CommandSender sender,@NotNull Command command,@NotNull String label,@NotNull String[] args){
        if(args.length!=1){sender.sendMessage(Component.text("사용법: /br <join|leave|start|stop|status>", NamedTextColor.YELLOW));return true;}
        String sub=args[0].toLowerCase();
        if((sub.equals("join")||sub.equals("leave")) && !(sender instanceof Player)){sender.sendMessage(GameManager.error("플레이어만 사용할 수 있습니다."));return true;}
        Component result=switch(sub){
            case "join" -> sender.hasPermission("battleroyale.play")?game.join((Player)sender):GameManager.error("권한이 없습니다.");
            case "leave" -> sender.hasPermission("battleroyale.play")?game.leave((Player)sender):GameManager.error("권한이 없습니다.");
            case "start" -> sender.hasPermission("battleroyale.admin") && sender instanceof Player p?game.start(p):GameManager.error("관리자 플레이어만 시작할 수 있습니다.");
            case "stop" -> { if(sender.hasPermission("battleroyale.admin")){game.stop("관리자가 게임을 강제 종료했습니다.");yield GameManager.ok("종료 요청을 처리했습니다.");} yield GameManager.error("권한이 없습니다."); }
            case "status" -> Component.text("상태: "+game.phase()+", 참가: "+game.joinedCount()+", 생존: "+game.aliveCount()+", 남은 시간: "+game.remainingSeconds()+"초",NamedTextColor.AQUA);
            default -> GameManager.error("알 수 없는 하위 명령어입니다.");};
        sender.sendMessage(result);return true;
    }
    @Override public List<String> onTabComplete(@NotNull CommandSender s,@NotNull Command c,@NotNull String a,@NotNull String[] args){
        if(args.length!=1)return List.of(); String q=args[0].toLowerCase(); return List.of("join","leave","start","stop","status").stream().filter(x->x.startsWith(q)).toList();
    }
}
