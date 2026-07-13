package dev.yayo.battleroyale;

final class PlacementRules {
    private PlacementRules() {}
    static boolean farEnough(double x1, double z1, double x2, double z2, double minimumDistance) {
        double dx=x1-x2, dz=z1-z2;
        return dx*dx+dz*dz >= minimumDistance*minimumDistance;
    }
}
