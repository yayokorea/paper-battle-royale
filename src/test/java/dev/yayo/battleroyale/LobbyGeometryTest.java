package dev.yayo.battleroyale;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class LobbyGeometryTest {
 @Test void 원_안쪽을_판정하되_네_끝의_돌출점은_제외한다(){assertTrue(LobbyGeometry.inside(0,0,8));assertFalse(LobbyGeometry.inside(8,0,8));assertFalse(LobbyGeometry.inside(0,-8,8));assertFalse(LobbyGeometry.inside(8,1,8));}
 @Test void 경계를_판정한다(){assertTrue(LobbyGeometry.boundary(7,0,8));assertFalse(LobbyGeometry.boundary(0,0,8));}
 @Test void 꺾인_경계의_연결점만_벽으로_판정한다(){assertTrue(LobbyGeometry.cornerConnector(6,3,8));assertFalse(LobbyGeometry.cornerConnector(6,0,8));assertTrue(LobbyGeometry.wall(7,0,8));assertFalse(LobbyGeometry.wall(0,0,8));}
 @Test void 추락_높이를_판정한다(){assertTrue(LobbyGeometry.below(190,200));assertFalse(LobbyGeometry.below(195,200));}
}
