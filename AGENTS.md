# 저장소 관리 지침

이 문서는 `/home/yayo/paper-battle-royale` 저장소에서 작업하는 사람과 자동화 에이전트가 따라야 할 운영 규칙이다.

## 기본 원칙

- 계획, 작업 설명, 커밋 및 PR 관련 안내는 한국어로 작성한다.
- `main`에 직접 커밋하거나 push하지 않는다.
- 작업마다 `main`에서 새 브랜치를 만들고 Pull Request를 통해 병합한다.
- 사용자가 만든 기존 변경사항과 작업 범위 밖의 파일은 수정, 삭제, 이동하거나 커밋에 포함하지 않는다.
- 강제 push, 브랜치 보호 우회, 태그 수동 덮어쓰기를 하지 않는다.
- 저장소의 병합 방식은 Squash merge만 사용한다.

## 브랜치와 Pull Request

작업 브랜치는 목적이 드러나는 짧은 이름을 사용한다.

```text
agent/fix-lobby-spawn
agent/improve-game-ui
agent/update-ci
```

PR 제목은 Conventional Commits 형식을 사용한다. CI가 다음 형식을 검사하며 Squash merge 후 이 제목이 `main`의 최종 커밋 제목이 된다.

```text
feat: 새로운 기능
fix: 버그 수정
docs: 문서 변경
refactor: 동작을 바꾸지 않는 코드 정리
perf: 성능 개선
test: 테스트 변경
build: 빌드 구성 변경
ci: GitHub Actions 변경
chore: 그 밖의 관리 작업
```

호환성이 깨지는 변경은 `!` 또는 `BREAKING CHANGE:`를 사용한다.

```text
feat!: 설정 파일 형식 변경
```

PR을 만들기 전에 변경 범위를 확인하고 관련 파일만 명시적으로 stage한다. PR 설명에는 변경 내용, 사용자 영향과 실행한 검증을 기록한다.

## 병합 규칙

`main`에는 다음 Ruleset이 적용된다.

- Pull Request 필수
- Squash merge만 허용
- Linear history 필수
- 브랜치 삭제 금지
- Force push 금지
- 필수 승인 수는 0명

PR의 CI가 성공한 것을 확인한 뒤 Squash merge한다. 여러 작업을 하나의 PR에 섞지 않는다. Release Please가 만든 릴리스 PR에도 기능 수정이나 별도 문서 변경을 추가하지 않는다.

## 빌드와 테스트

프로젝트는 Java 25와 Gradle Wrapper를 사용한다. 변경 후 다음 명령으로 전체 빌드를 확인한다.

```bash
./gradlew --no-daemon clean build
```

로컬 기본 Java가 25가 아니라면 Java 25의 `JAVA_HOME`과 `PATH`를 해당 명령에만 지정한다. 전역 Java 설정은 임의로 변경하지 않는다.

PR과 `main` push에서는 `.github/workflows/ci.yml`이 다음을 수행한다.

- PR 제목의 Conventional Commits 형식 검사
- Java 25 전체 Gradle 빌드와 테스트
- 플러그인 JAR와 테스트 리포트 업로드

CI 실패 상태로 병합하지 않는다. 실패하면 해당 실행의 로그에서 실제 실패 단계를 확인하고 원인을 수정한다.

## 버전과 릴리스

릴리스는 Release Please가 관리한다.

- 현재 버전의 단일 원본은 `version.txt`이다.
- `build.gradle.kts`는 `version.txt`를 읽는다.
- `plugin.yml`의 버전은 Gradle 리소스 처리 과정에서 같은 값으로 생성된다.
- `.release-please-manifest.json`, `version.txt`, `CHANGELOG.md`의 릴리스 버전을 평상시 수동으로 올리지 않는다.
- Git 태그와 GitHub Release를 평상시 수동 생성하지 않는다.

일반 PR이 `main`에 병합되면 Release Please가 Conventional Commit을 분석해 하나의 릴리스 PR을 생성하거나 기존 릴리스 PR을 갱신한다.

```text
fix:  → patch, 예: 1.1.0 → 1.1.1
feat: → minor, 예: 1.1.0 → 1.2.0
feat!: 또는 BREAKING CHANGE: → major, 예: 1.1.0 → 2.0.0
```

여러 일반 PR이 병합되어도 실제 릴리스 전까지 변경사항은 하나의 릴리스 PR에 누적된다. 배포할 준비가 되었을 때 `chore(main): release x.y.z` PR의 버전과 CHANGELOG를 검토하고 Squash merge한다.

릴리스 PR이 병합되면 `.github/workflows/release.yml`이 다음을 수행한다.

- `vx.y.z` 태그와 GitHub Release 생성
- 해당 태그를 체크아웃하여 Java 25로 다시 빌드
- `WildBattleRoyale-x.y.z.jar` 첨부
- `checksums.txt`에 SHA-256 체크섬을 생성해 첨부

릴리스 작업이 끝나면 GitHub Release의 태그, JAR 이름, 체크섬과 Actions 성공 여부를 확인한다.

## Dependabot

Dependabot은 매주 Gradle과 GitHub Actions 업데이트를 그룹 PR로 만든다.

- Dependabot PR도 일반 PR과 동일하게 CI 성공 후 병합한다.
- Paper API, Java, Gradle 또는 JUnit의 큰 버전 변경은 변경 내용을 확인하고 빌드만 통과했다는 이유로 즉시 병합하지 않는다.
- GitHub Actions는 가능한 한 릴리스 태그가 가리키는 전체 커밋 SHA로 고정한다.
- 자동 병합은 사용하지 않는다.

의존성 그래프가 Gradle 빌드 도구의 간접 의존성을 프로젝트 의존성처럼 보고할 수 있다. `security_update_dependency_not_found`가 발생하면 먼저 해당 라이브러리가 `build.gradle.kts`에 직접 선언됐는지 확인한다. 직접 의존성이 아니라면 플러그인 런타임 취약점으로 단정하거나 임의의 직접 의존성을 추가하지 않는다.

## GitHub 저장소 설정

- 저장소는 공개 상태를 유지한다.
- Actions의 기본 권한은 `contents: read`로 유지한다.
- Release 워크플로에만 `contents: write`, `pull-requests: write`를 부여한다.
- 외부 Actions는 검토된 커밋 SHA로 고정한다.
- Dependency Graph와 Dependabot Security Updates를 유지한다.
- Ruleset `protect`는 활성 상태로 기본 브랜치에 적용한다.

Ruleset이나 Actions 권한을 변경할 때는 Release Please의 PR 생성과 태그 게시가 막히지 않는지 확인한다. `Restrict updates`, 필수 배포, 미구성 상태 검사, 미구성 코드 스캔을 무심코 필수화하지 않는다.

## 작업 완료 보고

작업을 마칠 때 다음을 간결하게 보고한다.

- 구현하거나 변경한 내용
- 생성·수정한 주요 파일
- 실행한 빌드와 테스트 결과
- 브랜치, 커밋, PR 및 병합 여부
- 릴리스가 있었다면 태그와 Release 주소
- 남은 제한사항이나 사람이 검토해야 할 항목
