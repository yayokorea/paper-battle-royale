package dev.yayo.battleroyale;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GameUiStateTest {
 @Test void 시간을_분초로_표시한다(){assertEquals("14:32",GameUiState.formatTime(872));}
 @Test void 음수_시간은_영으로_표시한다(){assertEquals("00:00",GameUiState.formatTime(-2));}
 @Test void 진행률은_범위를_벗어나지_않는다(){assertEquals(1,GameUiState.clampProgress(20,10));assertEquals(0,GameUiState.clampProgress(-1,10));}
 @Test void 정상_진행률을_계산한다(){assertEquals(.5,GameUiState.clampProgress(5,10));}
}
