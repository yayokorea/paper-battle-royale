package dev.yayo.battleroyale;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import java.util.*;

final class GameUiManager {
 private static final String[] ENTRIES={"§0","§1","§2","§3","§4","§5"};
 private final Map<UUID,Scoreboard> previousBoards=new HashMap<>(); private final Set<UUID> viewers=new HashSet<>();
 private BossBar bossBar; private Scoreboard board; private final List<Team> lines=new ArrayList<>();
 void start(Collection<UUID> participants,GameUiState state){
  bossBar=BossBar.bossBar(Component.empty(),1,BossBar.Color.YELLOW,BossBar.Overlay.PROGRESS);
  ScoreboardManager manager=Objects.requireNonNull(Bukkit.getScoreboardManager(),"스코어보드 관리자를 사용할 수 없습니다."); board=manager.getNewScoreboard();
  Objective objective=board.registerNewObjective("wild_br",Criteria.DUMMY,Component.text("WILD BATTLE ROYALE",NamedTextColor.GOLD)); objective.setDisplaySlot(DisplaySlot.SIDEBAR);
  for(int i=0;i<ENTRIES.length;i++){Team team=board.registerNewTeam("br_line_"+i);team.addEntry(ENTRIES[i]);lines.add(team);objective.getScore(ENTRIES[i]).setScore(ENTRIES.length-i);}
  participants.forEach(id->{Player p=Bukkit.getPlayer(id);if(p!=null)show(p);});update(state);
 }
 void show(Player p){if(bossBar==null||board==null)return;previousBoards.putIfAbsent(p.getUniqueId(),p.getScoreboard());viewers.add(p.getUniqueId());p.showBossBar(bossBar);p.setScoreboard(board);}
 void hideForQuit(Player p){viewers.remove(p.getUniqueId());if(bossBar!=null)p.hideBossBar(bossBar);}
 void update(GameUiState s){if(bossBar==null||lines.size()!=ENTRIES.length)return;boolean prep=s.phase()==GamePhase.PREPARING;
  bossBar.name(Component.text((prep?"준비 시간":"전투 진행")+" | "+GameUiState.formatTime(s.phaseRemainingSeconds()),prep?NamedTextColor.YELLOW:NamedTextColor.RED));bossBar.color(prep?BossBar.Color.YELLOW:BossBar.Color.RED);bossBar.progress((float)s.progress());
  line(0,Component.text("단계  ",NamedTextColor.GRAY).append(Component.text(prep?"준비":"전투",prep?NamedTextColor.YELLOW:NamedTextColor.RED)));
  line(1,Component.text("생존자  ",NamedTextColor.GRAY).append(Component.text(s.alive()+" / "+s.total(),NamedTextColor.WHITE)));
  line(2,Component.text("PvP  ",NamedTextColor.GRAY).append(Component.text(s.pvpEnabled()?"활성":"보호 중",s.pvpEnabled()?NamedTextColor.RED:NamedTextColor.GREEN)));
  line(3,Component.text("보더  ",NamedTextColor.GRAY).append(Component.text(Math.round(s.borderSize())+" 블록",NamedTextColor.AQUA)));
  line(4,Component.text("보더 상태  ",NamedTextColor.GRAY).append(Component.text(s.borderState(),NamedTextColor.WHITE)));
  line(5,Component.text("경기 잔여  ",NamedTextColor.GRAY).append(Component.text(GameUiState.formatTime(s.matchRemainingSeconds()),NamedTextColor.WHITE)));
 }
 private void line(int i,Component c){lines.get(i).prefix(c);}
 void stop(){for(UUID id:new HashSet<>(viewers)){Player p=Bukkit.getPlayer(id);if(p==null)continue;if(bossBar!=null)p.hideBossBar(bossBar);Scoreboard old=previousBoards.get(id);if(old!=null)p.setScoreboard(old);}viewers.clear();previousBoards.clear();lines.clear();board=null;bossBar=null;}
}
