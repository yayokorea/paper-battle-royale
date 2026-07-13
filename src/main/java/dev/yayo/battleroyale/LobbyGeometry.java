package dev.yayo.battleroyale;

final class LobbyGeometry {
    private LobbyGeometry() {}
    static boolean inside(int x,int z,int radius){return (long)x*x+(long)z*z<=(long)radius*radius;}
    static boolean boundary(int x,int z,int radius){return inside(x,z,radius)&&(!inside(x+1,z,radius)||!inside(x-1,z,radius)||!inside(x,z+1,radius)||!inside(x,z-1,radius));}
    static boolean below(double y,double floorY){return y<floorY-8;}
}
