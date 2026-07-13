package dev.yayo.battleroyale;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlacementRulesTest {
    @Test void 경계거리도_허용한다() { assertTrue(PlacementRules.farEnough(0,0,300,400,500)); }
    @Test void 최소거리보다_가까우면_거부한다() { assertFalse(PlacementRules.farEnough(0,0,299,400,500)); }
    @Test void 음수좌표에서도_동작한다() { assertTrue(PlacementRules.farEnough(-100,-100,100,100,280)); }
}
