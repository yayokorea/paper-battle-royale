package dev.yayo.battleroyale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class GameManager {
    private final BattleRoyalePlugin plugin;
    private final GameConfig config;
    private final Set<UUID> joined = new LinkedHashSet<>(), alive = new LinkedHashSet<>(), eliminated = new HashSet<>();
    private final Map<UUID, PlayerSnapshot> snapshots = new HashMap<>();
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final GameUiManager ui = new GameUiManager();
    private GamePhase phase = GamePhase.IDLE;
    private World world;
    private BorderSnapshot borderSnapshot;
    private long startedAt;
    private int initialPlayers;

    GameManager(BattleRoyalePlugin plugin, GameConfig config) { this.plugin = plugin; this.config = config; }
    public GamePhase phase() { return phase; }
    public int joinedCount() { return joined.size(); }
    public int aliveCount() { return alive.size(); }
    public long remainingSeconds() {
        return phase == GamePhase.IDLE ? 0 : Math.max(0, config.maxMatchSeconds() - (System.currentTimeMillis()-startedAt)/1000);
    }
    public boolean isParticipant(UUID id) { return joined.contains(id); }
    public boolean isAlive(UUID id) { return alive.contains(id); }
    public boolean pvpEnabled() { return phase == GamePhase.ACTIVE; }

    public Component join(Player p) {
        if (phase != GamePhase.IDLE) return error("이미 게임이 진행 중입니다.");
        if (joined.contains(p.getUniqueId())) return error("이미 참가했습니다.");
        if (joined.size() >= config.maxPlayers()) return error("참가 인원이 가득 찼습니다.");
        joined.add(p.getUniqueId());
        broadcast(p.name().append(Component.text(" 님이 참가했습니다. ("+joined.size()+"/"+config.maxPlayers()+")", NamedTextColor.YELLOW)));
        return ok("배틀로얄에 참가했습니다.");
    }
    public Component leave(Player p) {
        if (phase != GamePhase.IDLE) return error("진행 중에는 참가 취소할 수 없습니다.");
        if (!joined.remove(p.getUniqueId())) return error("참가 중이 아닙니다.");
        return ok("참가를 취소했습니다.");
    }
    public Component start(Player sender) {
        if (phase != GamePhase.IDLE) return error("이미 게임이 진행 중입니다.");
        joined.removeIf(id -> Bukkit.getPlayer(id) == null);
        if (joined.size() < config.minPlayers()) return error("최소 "+config.minPlayers()+"명이 필요합니다.");
        world = sender.getWorld();
        if (!joined.stream().map(Bukkit::getPlayer).allMatch(p -> p != null && p.getWorld().equals(world)))
            return error("모든 참가자는 시작한 관리자와 같은 월드에 있어야 합니다.");
        phase = GamePhase.PREPARING; startedAt = System.currentTimeMillis();
        borderSnapshot = BorderSnapshot.capture(world.getWorldBorder());
        joined.forEach(id -> { Player p=Bukkit.getPlayer(id); snapshots.put(id, PlayerSnapshot.capture(p)); });
        alive.addAll(joined); initialPlayers=alive.size();
        try {
            WorldBorder b=world.getWorldBorder(); b.setCenter(sender.getLocation()); b.setSize(config.borderStartSize());
            preparePlayers(sender.getLocation()).whenComplete((v, ex) -> Bukkit.getScheduler().runTask(plugin, () -> {
                if (ex != null) { plugin.getLogger().severe("시작 텔레포트 실패: "+ex.getMessage()); stop("시작 중 오류가 발생했습니다."); }
                else if (phase == GamePhase.PREPARING) beginTimers();
            }));
        } catch (RuntimeException e) { plugin.getLogger().severe("게임 시작 실패: "+e.getMessage()); stop("시작 중 오류가 발생했습니다."); }
        return ok("안전한 시작 위치를 찾는 중입니다.");
    }
    private CompletableFuture<Void> preparePlayers(Location center) {
        List<Location> locations = findLocations(center);
        if (locations.size() < alive.size()) return CompletableFuture.failedFuture(new IllegalStateException("서로 충분히 떨어진 안전 위치 부족"));
        List<CompletableFuture<Boolean>> futures = new ArrayList<>(); int i=0;
        for (UUID id : alive) {
            Player p=Bukkit.getPlayer(id); Location loc=locations.get(i++);
            p.getInventory().clear(); p.getInventory().setArmorContents(null); p.setGameMode(GameMode.SURVIVAL);
            p.setHealth(p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()); p.setFoodLevel(20); p.setSaturation(5);
            futures.add(world.getChunkAtAsync(loc).thenCompose(chunk -> p.teleportAsync(loc)));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }
    private List<Location> findLocations(Location center) {
        List<Location> result=new ArrayList<>(); Random random=new Random(); int total=config.randomLocationAttempts()*alive.size();
        for(int n=0;n<total && result.size()<alive.size();n++) {
            double angle=random.nextDouble()*Math.PI*2, radius=Math.sqrt(random.nextDouble())*config.teleportRadius();
            int x=(int)Math.floor(center.getX()+Math.cos(angle)*radius), z=(int)Math.floor(center.getZ()+Math.sin(angle)*radius);
            int y=world.getHighestBlockYAt(x,z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
            Location loc=new Location(world,x+0.5,y+1,z+0.5); Block ground=loc.getBlock().getRelative(BlockFace.DOWN);
            if (!ground.getType().isSolid() || ground.isLiquid() || loc.getBlock().getType().isSolid() || loc.clone().add(0,1,0).getBlock().getType().isSolid()) continue;
            if (result.stream().anyMatch(other -> !PlacementRules.farEnough(other.getX(), other.getZ(), loc.getX(), loc.getZ(), config.minimumPlayerDistance()))) continue;
            result.add(loc);
        }
        return result;
    }
    private void beginTimers() {
        ui.start(joined, uiState());
        tasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> ui.update(uiState()), 0L, 20L));
        broadcast(Component.text("준비 시작! "+config.preparationSeconds()+"초 동안 PvP가 차단됩니다.", NamedTextColor.GOLD));
        tasks.add(Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (phase == GamePhase.PREPARING) { phase=GamePhase.ACTIVE; broadcast(Component.text("PvP가 활성화되었습니다!", NamedTextColor.RED)); }
        }, secondsToTicks(config.preparationSeconds())));
        tasks.add(Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (phase==GamePhase.PREPARING || phase==GamePhase.ACTIVE) {
                world.getWorldBorder().changeSize(config.borderEndSize(), secondsToTicks(config.borderShrinkDurationSeconds()));
                broadcast(Component.text("월드보더가 축소되기 시작합니다.", NamedTextColor.RED));
            }
        }, secondsToTicks(config.borderShrinkDelaySeconds())));
        tasks.add(Bukkit.getScheduler().runTaskLater(plugin, () -> stop("최대 경기 시간이 지나 무승부로 종료합니다."), secondsToTicks(config.maxMatchSeconds())));
    }
    public void eliminate(Player p, String reason) {
        UUID id=p.getUniqueId(); if (!alive.remove(id)) return; eliminated.add(id);
        p.setGameMode(GameMode.SPECTATOR); broadcast(Component.text(p.getName()+" 탈락: "+reason+" (생존 "+alive.size()+"명)", NamedTextColor.RED));
        checkWinner();
    }
    public void handleJoin(Player p) {
        UUID id=p.getUniqueId();
        if (eliminated.contains(id) && phase != GamePhase.IDLE) { p.setGameMode(GameMode.SPECTATOR); ui.show(p); }
        else if (snapshots.containsKey(id) && phase == GamePhase.IDLE) { snapshots.remove(id).restore(p); joined.remove(id); eliminated.remove(id); }
    }
    private void checkWinner() {
        if ((phase==GamePhase.PREPARING || phase==GamePhase.ACTIVE) && alive.size() <= 1) {
            String msg=alive.isEmpty()?"생존자가 없어 무승부입니다.":Bukkit.getOfflinePlayer(alive.iterator().next()).getName()+" 님이 승리했습니다!";
            stop(msg);
        }
    }
    public void stop(String reason) {
        if (phase==GamePhase.IDLE || phase==GamePhase.ENDING) return;
        phase=GamePhase.ENDING; broadcast(Component.text(reason, NamedTextColor.GOLD)); tasks.forEach(BukkitTask::cancel); tasks.clear(); ui.stop();
        if (world!=null && borderSnapshot!=null) try { borderSnapshot.restore(world.getWorldBorder()); } catch(RuntimeException e){ plugin.getLogger().severe("월드보더 복구 실패: "+e.getMessage()); }
        snapshots.forEach((id,s) -> { Player p=Bukkit.getPlayer(id); if(p!=null) try{s.restore(p);}catch(RuntimeException e){plugin.getLogger().warning(p.getName()+" 상태 복구 실패");} });
        joined.clear(); alive.clear(); eliminated.clear(); borderSnapshot=null; world=null; phase=GamePhase.IDLE;
    }
    public void shutdown() { stop("플러그인 비활성화로 게임을 종료합니다."); }
    public void handleQuit(Player p) { ui.hideForQuit(p); if(isAlive(p.getUniqueId())) eliminate(p,"게임 중 로그아웃"); }
    private GameUiState uiState() {
        long elapsed=Math.max(0,(System.currentTimeMillis()-startedAt)/1000), matchLeft=Math.max(0,config.maxMatchSeconds()-elapsed);
        long phaseLeft=phase==GamePhase.PREPARING?Math.max(0,config.preparationSeconds()-elapsed):matchLeft;
        long duration=phase==GamePhase.PREPARING?config.preparationSeconds():Math.max(1,config.maxMatchSeconds()-config.preparationSeconds());
        long borderStart=config.borderShrinkDelaySeconds(), borderEnd=borderStart+config.borderShrinkDurationSeconds();
        String borderState=elapsed<borderStart?"대기":elapsed<borderEnd?"축소 중":"최종 크기";
        double size=world==null?config.borderStartSize():world.getWorldBorder().getSize();
        return new GameUiState(phase,phaseLeft,matchLeft,alive.size(),initialPlayers,pvpEnabled(),size,borderState,GameUiState.clampProgress(phaseLeft,duration));
    }
    private void broadcast(Component c) { Bukkit.getServer().broadcast(c); }
    static Component ok(String s){return Component.text(s, NamedTextColor.GREEN);} static Component error(String s){return Component.text(s, NamedTextColor.RED);}
    private static long secondsToTicks(long seconds){return Math.multiplyExact(seconds,20L);}
}
