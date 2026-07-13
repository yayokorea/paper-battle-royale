package dev.yayo.battleroyale;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

final class LobbyManager {
    private final BattleRoyalePlugin plugin;
    private final String worldName;
    private final String ySetting;
    private final int radius,startY,step;
    private volatile Location spawn;
    private volatile boolean ready;

    LobbyManager(BattleRoyalePlugin plugin){
        this.plugin=plugin; worldName=plugin.getConfig().getString("lobby-world","world"); ySetting=plugin.getConfig().getString("lobby-y","auto");
        radius=plugin.getConfig().getInt("lobby-radius",8); startY=plugin.getConfig().getInt("lobby-search-start-y",200);
        step=plugin.getConfig().getInt("lobby-search-step",4);
        if(radius<5||radius>24||step<1)throw new IllegalArgumentException("로비 반지름 또는 탐색 간격이 잘못되었습니다.");
    }
    CompletableFuture<Void> initialize(){
        World world=Bukkit.getWorld(worldName);
        if(world==null)return CompletableFuture.failedFuture(new IllegalStateException("로비 월드를 찾을 수 없습니다: "+worldName));
        return world.getChunkAtAsync(0,0,true).thenCompose(chunk->{
            CompletableFuture<Void> result=new CompletableFuture<>();
            Bukkit.getScheduler().runTask(plugin,()->{
                try { int y=loadOrFindY(world); build(world,y); activate(world,y); result.complete(null); }
                catch(Exception e){result.completeExceptionally(e);}
            }); return result;
        });
    }
    private int loadOrFindY(World world) throws IOException {
        File file=new File(plugin.getDataFolder(),"lobby-location.yml");
        if(file.isFile()){
            YamlConfiguration data=YamlConfiguration.loadConfiguration(file);
            if(world.getName().equals(data.getString("world"))){
                int y=data.getInt("y",Integer.MIN_VALUE);
                if(validY(world,y)&&isExistingLobby(world,y))return y;
                throw new IllegalStateException("저장된 로비 위치가 변경되었거나 손상되었습니다. lobby-location.yml을 확인하세요.");
            }
        }
        int y;
        if(ySetting.equalsIgnoreCase("auto"))y=findEmptyY(world);
        else {
            try{y=Integer.parseInt(ySetting);}catch(NumberFormatException e){throw new IllegalArgumentException("lobby-y는 auto 또는 정수여야 합니다.");}
            if(!validY(world,y)||(!isExistingLobby(world,y)&&!emptyVolume(world,y)))throw new IllegalStateException("수동 lobby-y 위치가 높이 범위를 벗어나거나 기존 블록과 충돌합니다: "+y);
        }
        if(y==Integer.MIN_VALUE)throw new IllegalStateException("0,0 상공에서 로비를 만들 빈 공간을 찾지 못했습니다.");
        plugin.getDataFolder().mkdirs(); YamlConfiguration data=new YamlConfiguration(); data.set("world",world.getName()); data.set("y",y); data.save(file); return y;
    }
    private int findEmptyY(World world){
        int min=world.getMinHeight()+12,max=world.getMaxHeight()-8,base=Math.max(min,Math.min(max,startY));
        for(int distance=0;distance<=Math.max(max-base,base-min);distance+=step){
            int up=base+distance;if(up<=max&&emptyVolume(world,up))return up;
            int down=base-distance;if(distance>0&&down>=min&&emptyVolume(world,down))return down;
        } return Integer.MIN_VALUE;
    }
    private boolean validY(World world,int y){return y>=world.getMinHeight()+12&&y<=world.getMaxHeight()-8;}
    private boolean emptyVolume(World world,int y){
        int extent=radius+2;
        for(int x=-extent;x<=extent;x++)for(int z=-extent;z<=extent;z++)for(int dy=-4;dy<=6;dy++)if(!world.getBlockAt(x,y+dy,z).getType().isAir())return false;
        return true;
    }
    private boolean isExistingLobby(World world,int y){return validY(world,y)&&world.getBlockAt(0,y-2,0).getType()==Material.LODESTONE&&world.getBlockAt(0,y,0).getType()==Material.CHISELED_STONE_BRICKS;}
    private void build(World world,int y){
        clearTips(world,y);
        for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++)if(LobbyGeometry.inside(x,z,radius)){
            material(world,x,y-2,z,Material.STONE_BRICKS); material(world,x,y-1,z,Material.SMOOTH_STONE);
            material(world,x,y,z,(Math.abs(x)<=1||Math.abs(z)<=1)?Material.CHISELED_STONE_BRICKS:Material.SPRUCE_PLANKS);
            if(LobbyGeometry.wall(x,z,radius)){material(world,x,y+1,z,Material.GLASS_PANE);material(world,x,y+2,z,Material.GLASS_PANE);}
            else {clearPane(world,x,y+1,z);clearPane(world,x,y+2,z);}
        }
        material(world,0,y-2,0,Material.LODESTONE); material(world,0,y,0,Material.CHISELED_STONE_BRICKS);
        int p=radius-2; for(int[] pos:new int[][]{{p,0},{-p,0},{0,p},{0,-p}}){material(world,pos[0],y+1,pos[1],Material.SPRUCE_FENCE);material(world,pos[0],y+2,pos[1],Material.LANTERN);}
        connectPanes(world,y+1);connectPanes(world,y+2);
    }
    private void material(World w,int x,int y,int z,Material m){w.getBlockAt(x,y,z).setType(m,m==Material.GLASS_PANE);}
    private void clearPane(World w,int x,int y,int z){Block block=w.getBlockAt(x,y,z);if(block.getType()==Material.GLASS_PANE)block.setType(Material.AIR,true);}
    private void clearTips(World world,int y){
        for(int[] pos:new int[][]{{radius,0},{-radius,0},{0,radius},{0,-radius}})
            for(int dy=-2;dy<=2;dy++)world.getBlockAt(pos[0],y+dy,pos[1]).setType(Material.AIR,false);
    }
    private void connectPanes(World world,int y){
        for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++){
            Block block=world.getBlockAt(x,y,z);
            if(block.getType()!=Material.GLASS_PANE||!(block.getBlockData() instanceof MultipleFacing pane))continue;
            for(BlockFace face:new BlockFace[]{BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST})
                pane.setFace(face,connectsToPane(block.getRelative(face)));
            block.setBlockData(pane,false);
        }
    }
    private boolean connectsToPane(Block block){return block.getType()==Material.GLASS_PANE||block.getType()==Material.SPRUCE_FENCE;}
    private void activate(World world,int y){spawn=new Location(world,.5,y+1,.5,0,0);world.setSpawnLocation(0,y+1,0,0);ready=true;plugin.getLogger().info("하늘 로비 준비 완료: "+world.getName()+" 0,"+y+",0");}
    boolean ready(){return ready;}
    Location spawn(){if(!ready)throw new IllegalStateException("로비가 아직 준비되지 않았습니다.");return spawn.clone();}
    World world(){return spawn().getWorld();}
    String description(){if(!ready)return "준비 중";Location l=spawn();return l.getWorld().getName()+" 0,"+(l.getBlockY()-1)+",0";}
    void teleport(Player p){if(!ready)return;p.teleportAsync(spawn());}
    void prepareWaitingPlayer(Player p){if(!ready)return;p.setGameMode(GameMode.ADVENTURE);p.setAllowFlight(false);p.setFlying(false);p.setFoodLevel(20);teleport(p);}
    boolean inLobby(Location l){if(!ready||l.getWorld()!=spawn.getWorld())return false;double dx=l.getX()-.5,dz=l.getZ()-.5;return Math.abs(l.getY()-(spawn.getY()-1))<=8&&dx*dx+dz*dz<=(radius+3d)*(radius+3d);}
    boolean fellFromLobby(Location l){return ready&&l.getWorld()==spawn.getWorld()&&LobbyGeometry.below(l.getY(),spawn.getY()-1)&&Math.abs(l.getX())<radius+8&&Math.abs(l.getZ())<radius+8;}
}
