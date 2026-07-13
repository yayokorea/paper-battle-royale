package dev.yayo.battleroyale;

final class LobbyGeometry {
    private LobbyGeometry() {}
    static boolean inside(int x,int z,int radius){
        if((Math.abs(x)==radius&&z==0)||(Math.abs(z)==radius&&x==0))return false;
        return (long)x*x+(long)z*z<=(long)radius*radius;
    }
    static boolean boundary(int x,int z,int radius){return inside(x,z,radius)&&(!inside(x+1,z,radius)||!inside(x-1,z,radius)||!inside(x,z+1,radius)||!inside(x,z-1,radius));}
    static boolean cornerConnector(int x,int z,int radius){
        if(!inside(x,z,radius)||boundary(x,z,radius))return false;
        boolean xEdge=boundary(x+1,z,radius)||boundary(x-1,z,radius);
        boolean zEdge=boundary(x,z+1,radius)||boundary(x,z-1,radius);
        return xEdge&&zEdge;
    }
    static boolean wall(int x,int z,int radius){return boundary(x,z,radius)||cornerConnector(x,z,radius);}
    static boolean below(double y,double floorY){return y<floorY-8;}
}
