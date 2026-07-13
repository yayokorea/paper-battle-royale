package dev.yayo.battleroyale;
record GameUiState(GamePhase phase,long phaseRemainingSeconds,long matchRemainingSeconds,int alive,int total,boolean pvpEnabled,double borderSize,String borderState,double progress){
 static String formatTime(long seconds){long safe=Math.max(0,seconds);return "%02d:%02d".formatted(safe/60,safe%60);}
 static double clampProgress(long remaining,long duration){if(duration<=0)return 0;return Math.max(0,Math.min(1,(double)remaining/duration));}
}
