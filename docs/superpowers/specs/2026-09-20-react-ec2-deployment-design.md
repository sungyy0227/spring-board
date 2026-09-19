# React EC2 배포 설계

## 목표

현재 EC2에서 Spring Boot가 Thymeleaf 화면까지 응답하는 구조를 React SPA와 JSON API가 함께 동작하는 구조로 전환한다. 운영 도메인과 HTTPS 인증서는 그대로 사용하고, 정적 파일은 Nginx가 직접 제공하며 Spring Boot는 API, 업로드 이미지, WebSocket만 처리한다.

## 배포 구조

요청은 호스트 Nginx에서 다음과 같이 분기한다.

- `/api/` → `http://127.0.0.1:8080`의 Spring Boot
- `/images/` → `http://127.0.0.1:8080`의 Spring Boot
- `/ws`, `/ws/**` → `http://127.0.0.1:8080`의 Spring WebSocket
- 그 밖의 경로 → React 빌드 결과

React Router의 직접 접근과 새로고침을 지원하기 위해 정적 파일이 없으면 `index.html`로 되돌린다. 프론트와 API는 같은 도메인을 사용하므로 기존 `JSESSIONID`, CSRF 쿠키와 `credentials: "include"` 흐름을 유지한다.

## 빌드와 배포

- Spring Boot는 기존 `docker compose up -d --build`로 빌드하고 실행한다.
- React는 서버에 Node.js를 직접 설치하지 않고 `node:24-alpine` 일회성 컨테이너에서 `npm ci`와 `npm run build`를 실행한다.
- 빌드 결과는 `/var/www/spring-board/releases/<배포시각>`에 복사한다.
- `/var/www/spring-board/current` 심볼릭 링크를 새 릴리스로 원자적으로 교체한다.
- Nginx 설정 검증(`nginx -t`)이 성공한 뒤에만 reload한다.

## WebSocket

`/ws` 프록시는 HTTP/1.1과 `Upgrade`, `Connection` 헤더를 명시한다. 이 설정은 `ChatRoomPage`가 현재 호스트의 `/ws`로 연결하는 동작과 맞춘다.

## 안전장치와 롤백

- 첫 변경 전 현재 Nginx 설정을 타임스탬프가 붙은 파일로 백업한다.
- 서버 저장소의 기존 배포 커밋을 로컬 백업 브랜치로 남긴다.
- 새 Nginx 설정은 설치 전에 별도 임시 경로에서 준비하고, 설치 후 `nginx -t`로 확인한다.
- 정적 파일 롤백은 `current` 링크를 직전 릴리스로 되돌리면 된다.
- 애플리케이션 롤백은 백업 브랜치 또는 이전 커밋으로 전환한 뒤 Docker 이미지를 다시 빌드한다.
- 비밀키, `.env`, 인증서 내용은 Git에 추가하거나 출력하지 않는다.

## 검증 기준

- 백엔드 전체 테스트가 통과한다.
- 프론트 테스트, 린트, 프로덕션 빌드가 통과한다.
- 운영 서버에서 두 컨테이너가 정상 상태다.
- 운영 도메인 `/`이 React HTML을 반환한다.
- React의 깊은 경로가 서버 404가 아니라 `index.html`을 반환한다.
- `/api/v1/csrf`가 Spring Boot 응답을 반환한다.
- Nginx 설정 검증과 reload가 성공한다.
