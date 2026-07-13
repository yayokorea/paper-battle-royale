# WildBattleRoyale

Paper 26.2용 독립형 개인전 배틀로얄 플러그인입니다. 별도 로비나 아이템 시스템 없이 일반 야생 월드에서 파밍한 뒤 최후의 1인을 가립니다.

## 요구 환경과 설치

- Minecraft Java Edition / Paper 26.2 (`paper-api 26.2.build.60-beta` 기준)
- Java 25

`build/libs/WildBattleRoyale-1.0.0.jar`를 서버의 `plugins/`에 넣고 서버를 재시작합니다. Paper 26.2는 현재 beta 계열이므로 서버와 API 빌드를 함께 갱신할 때 변경 기록을 확인하십시오.

직접 빌드하려면 Java 25에서 다음을 실행합니다.

```bash
./gradlew clean build
```

## 사용법

- `/br join`: 참가 (`battleroyale.play`, 기본 허용)
- `/br leave`: 시작 전 참가 취소
- `/br start`: 명령 실행자의 현재 월드에서 시작 (`battleroyale.admin`, 기본 OP)
- `/br stop`: 강제 종료와 복구
- `/br status`: 단계, 참가/생존 인원, 남은 시간 확인

## 경기 UI

경기 참가자와 탈락한 관전자에게 시각 UI를 제공합니다. 화면 위 보스바에는 현재 단계와 단계 남은 시간이 표시되고, 우측 사이드바에는 생존자 수, PvP 상태, 현재 월드보더 크기, 보더 축소 상태와 전체 경기 잔여 시간이 1초마다 갱신됩니다. 경기 중에는 참가자의 기존 스코어보드를 임시로 교체하며 정상 종료, 강제 종료, 시작 오류 또는 플러그인 비활성화 시 원래 스코어보드로 복원합니다. 일반 서버 플레이어에게는 표시하지 않습니다.

시작할 때 모든 참가자는 시작 관리자와 같은 월드에 온라인 상태여야 합니다. 기존 위치, 게임 모드, 인벤토리/방어구/보조 손, 체력, 허기, 경험치, 비행 상태와 월드보더를 저장합니다. 참가자는 보더 중심 주변의 서로 떨어진 안전한 지상으로 이동하며 인벤토리를 비우고 생존 모드로 시작합니다. 준비 시간에는 참가자 간 직접·투사체 피해가 차단됩니다. 사망 또는 경기 중 로그아웃은 즉시 탈락이며 재접속하면 관전자입니다. 종료 시 온라인 참가자는 즉시, 오프라인 참가자는 다음 접속 때 저장 상태로 복구됩니다.

## 설정 (`plugins/WildBattleRoyale/config.yml`)

| 키 | 기본값 | 설명 |
|---|---:|---|
| `min-players` / `max-players` | 2 / 12 | 최소·최대 인원 |
| `preparation-seconds` | 900 | PvP 차단 시간 |
| `max-match-seconds` | 3600 | 최대 경기 시간 |
| `teleport-radius` | 1500 | 보더 중심 기준 무작위 반경 |
| `minimum-player-distance` | 350 | 시작 위치 최소 거리 |
| `border-start-size` / `border-end-size` | 3500 / 75 | 보더 시작·최종 한 변 길이 |
| `border-shrink-delay-seconds` | 900 | 시작 후 축소 개시 시점 |
| `border-shrink-duration-seconds` | 2400 | 축소에 걸리는 시간 |
| `random-location-attempts` | 80 | 참가자당 위치 탐색 시도 수 |

설정은 서버 시작 시 검증됩니다. `border-shrink-delay-seconds`는 준비 시간 이상이어야 하고 최대 경기 시간은 준비 시간보다 길어야 합니다.

## 안전 및 제한사항

- 안전 위치는 `MOTION_BLOCKING_NO_LEAVES` 최고 지형, 단단하고 액체가 아닌 바닥, 머리 공간을 검사합니다. 구조물 내부, 월드 생성기의 특수 위험, 인접 용암까지 완벽히 판별하지는 않습니다.
- 시작 위치 탐색 자체의 높이 조회는 서버 메인 스레드에서 실행되므로 매우 큰 인원/반경에서는 순간 부하가 생길 수 있습니다. 선택된 청크는 `getChunkAtAsync`로 로드한 후 `teleportAsync`로 이동합니다.
- 참가자가 죽을 때 드롭은 바닐라 규칙을 그대로 따릅니다. 원래 상태는 경기 종료 후 별도로 복원됩니다.
- 플러그인 비활성화, 강제 종료, 시작 오류에는 예약 작업과 보더를 취소·복구합니다. 서버 프로세스가 비정상 종료되어 `onDisable`이 실행되지 않으면 메모리 내 스냅샷은 복구할 수 없습니다.
- 별도 월드, 팀, 보급품, 클래스, 데이터베이스는 의도적으로 제공하지 않습니다.

## 확인한 Paper 26.2 API

Context7의 공식 `/websites/jd_papermc_io_paper_26_2` JavaDoc과 실제 컴파일로 `JavaPlugin`, `PluginCommand`, 스케줄러와 `BukkitTask`, `WorldBorder.changeSize(double,long)`(tick 단위), `getChunkAtAsync`, `teleportAsync`, `PlayerDeathEvent`/리스폰·접속 이벤트, `GameMode.SPECTATOR`, Adventure `Component` 메시지를 확인했습니다. deprecated 된 초 단위 `WorldBorder.setSize`와 문자열 사망 메시지 API는 사용하지 않습니다.
