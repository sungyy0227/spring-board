# React EC2 Deployment Implementation Plan

> **For Codex:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 현재 채팅 기능을 검증하고 기능 단위로 커밋한 뒤 `main`에 병합·푸시하고, EC2의 Nginx가 React SPA를 제공하도록 안전하게 배포한다.

**Architecture:** Nginx가 React 정적 파일과 SPA fallback을 처리하고 `/api/`, `/images/`, `/ws`만 Docker의 Spring Boot로 프록시한다. React는 일회성 Node Docker 컨테이너에서 빌드하며 정적 릴리스 디렉터리와 `current` 심볼릭 링크로 교체한다.

**Tech Stack:** Spring Boot 3, Java 17, Gradle, React 19, Vite 8, Vitest, Docker Compose, Nginx, Ubuntu EC2.

**Spec:** `docs/superpowers/specs/2026-09-20-react-ec2-deployment-design.md`

---

### Task 1: 초대 링크 참여 API 작업 검증 및 커밋

**Files:**
- Modify: `src/main/java/spring/board/chat/controller/ChatRoomInviteEntryApiController.java`
- Modify: `src/main/java/spring/board/chat/repository/ChatRoomMemberRepository.java`
- Modify: `src/main/java/spring/board/chat/service/ChatService.java`
- Modify: `src/main/java/spring/board/exception/ErrorCode.java`
- Create: `src/main/java/spring/board/chat/dto/ChatRoomInvitePreviewResponse.java`
- Create: `src/main/java/spring/board/chat/dto/ChatRoomJoinResponse.java`
- Test: `src/test/java/spring/board/chat/dto/ChatRoomJoinResponseTest.java`

1. 현재 diff를 검토하여 사용자 작성 로직을 보존하고 컴파일 경고를 만드는 미사용 import와 공백만 정리한다.
2. `./gradlew test`를 실행하여 전체 백엔드 회귀 검증을 한다.
3. `npm test`, `npm run lint`, `npm run build`를 실행하여 초대 링크 참여 화면을 포함한 프론트를 검증한다.
4. 관련 파일만 `feat: 초대 링크로 채팅방 참여 기능 추가`로 커밋한다.

### Task 2: 재현 가능한 React 운영 배포 구성 추가

**Files:**
- Create: `deploy/nginx/spring-board.conf`
- Create: `deploy/deploy.sh`
- Create: `docs/superpowers/specs/2026-09-20-react-ec2-deployment-design.md`
- Create: `docs/superpowers/plans/2026-09-20-react-ec2-deployment.md`

1. Nginx 정적 파일, SPA fallback, Spring API·이미지·WebSocket 프록시 설정을 작성한다.
2. Docker 기반 React 빌드, Spring Docker 재빌드, 버전 디렉터리 게시, 원자적 링크 교체, Nginx 검증·reload를 수행하는 스크립트를 작성한다.
3. `bash -n deploy/deploy.sh`와 로컬 프론트/백엔드 검증을 다시 실행한다.
4. 배포 구성과 문서를 `chore: React 운영 배포 구성 추가`로 커밋한다.

### Task 3: 기능 브랜치 최종 검토와 main 병합

**Files:**
- Review: `main..feature/chat`

1. 전체 변경과 테스트 결과를 새 관점에서 검토한다.
2. `main`으로 전환해 `feature/chat`을 `--no-ff`로 병합한다.
3. 병합된 `main`에서 백엔드 테스트와 프론트 테스트·린트·빌드를 다시 실행한다.
4. 원격이 예상대로라면 `main`을 일반 fast-forward push한다. 강제 push는 사용하지 않는다.

### Task 4: EC2 백업, 배포, 운영 검증

**Files:**
- Remote backup: `/etc/nginx/sites-enabled/spring-board.backup-<timestamp>`
- Remote install: `/etc/nginx/sites-available/spring-board`
- Remote releases: `/var/www/spring-board/releases/<timestamp>`

1. 서버의 현재 커밋, Git 상태, 컨테이너 상태, Node/Docker/Nginx 환경을 다시 확인한다.
2. 현재 Nginx 설정을 백업하고 서버 Git HEAD에 배포 전 백업 브랜치를 만든다.
3. 서버 `main`을 `git pull --ff-only origin main`으로 갱신한다.
4. 추적된 Nginx 설정을 설치한 후 `sudo nginx -t`로 검증한다. 실패하면 즉시 기존 설정을 복구한다.
5. `deploy/deploy.sh`를 실행하여 Spring과 React를 배포한다.
6. 컨테이너 상태, Nginx 상태, React 루트·깊은 경로·정적 자산·CSRF API를 smoke test한다.
7. 실제로 사용한 로컬/원격 명령과 각 명령의 목적, 백업 및 롤백 위치를 최종 보고한다.

## Review Focus

- Nginx SPA fallback이 `/api`, `/images`, `/ws` 요청을 가로채지 않는지
- WebSocket upgrade 헤더가 빠지지 않았는지
- 세션 쿠키와 CSRF가 같은 도메인 흐름을 유지하는지
- 실패 시 기존 Nginx 설정과 이전 정적 릴리스로 복구 가능한지
- 원격 `.env`, 인증서, 개인키를 읽거나 Git에 포함하지 않는지
