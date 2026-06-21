# 📝 Spring Board Project

Spring Boot와 Thymeleaf를 사용해 구현한 게시판 프로젝트입니다.  
회원/비회원 작성자를 구분하고, 게시글·댓글 권한 검증, 관리자 기능, 회원탈퇴까지 포함한 기본 커뮤니티 구조를 구현했습니다.

---

## 🚀 주요 기능

- 회원 / 비회원 게시글·댓글 작성 및 권한 검증
- 회원가입 / Spring Security 기반 로그인·로그아웃 및 URL 접근 제어
- Toast UI Editor 기반 게시글 작성 / 수정과 본문 이미지 업로드
- 게시글 검색, 페이징, 조회수 증가
- 마이페이지, 회원탈퇴, 관리자 회원 관리

---

## 🏗 기술 스택

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- Thymeleaf
- H2 Database
- Gradle

## 📚 사용 라이브러리

- Toast UI Editor
- OWASP Java HTML Sanitizer
- Apache Tika

---

## 🔧 구현 포인트

### 1. 회원 / 비회원 / 관리자 권한 모델

게시글과 댓글의 작성 주체를 회원과 비회원으로 나누고, 작성 주체에 따라 다른 권한 검증 방식을 적용했습니다.

- 회원 작성 글/댓글은 로그인 회원의 id와 작성자의 member_id를 비교
- 비회원 작성 글/댓글은 작성 시 입력한 비밀번호를 BCrypt로 저장하고 수정/삭제 시 검증
- 비회원 게시글 수정은 수정 화면 진입과 실제 수정 요청에서 모두 권한을 확인
- 회원탈퇴는 회원 상태를 `WITHDRAWN`으로 변경하여 기존 게시글·댓글의 작성자 관계를 유지
- 관리자는 일반 사용자의 게시글·댓글 관리와 회원 권한 변경 가능

---

### 2. Spring Security 기반 인증 / 인가 전환

이후 인증은 Spring Security의 `formLogin`으로, URL 접근 제어는 `authorizeHttpRequests`로 이전하고, 컨트롤러는 `@AuthenticationPrincipal`을 통해 로그인 사용자 id만 서비스 계층에 전달하도록 정리했습니다.

- `CustomUserDetailsService`에서 `loginId` 기준으로 회원을 조회
- 로그인 폼은 별도 DTO 바인딩 없이 `loginId`, `password` 파라미터를 Spring Security 설정과 맞춰 전송
- 로그인 사용자 정보는 컨트롤러에서 `@AuthenticationPrincipal`을 통해 조회
- 서비스 계층이 Security 인증 객체에 의존하지 않도록 컨트롤러에서 로그인 회원 id만 추출해 전달
- `/admin`, `/clearAll`, `/clearPost`는 관리자만 접근 가능
- `/mypage`, `/mypage/**`는 로그인 사용자만 접근 가능
- 인증되지 않은 사용자가 보호 URL에 접근하면 로그인 페이지로 이동
- 인증은 되었지만 권한이 부족한 사용자는 메인 페이지로 이동
- CSRF 보호를 활성화하고 form 요청과 이미지 업로드 fetch 요청에 CSRF 토큰을 포함
- 관리자 권한 변경 후 현재 로그인 사용자의 인증 정보를 `HttpSessionSecurityContextRepository`로 갱신

---

### 3. Toast UI Editor와 이미지 업로드 처리

게시글 본문을 HTML로 저장하기 위해 Toast UI Editor를 적용하고, 에디터 본문 이미지 업로드와 게시글 매핑 흐름을 분리했습니다.

- 작성 / 수정 화면에서 Toast UI Editor를 초기화하고 제출 시 HTML을 `PostDto.content`로 전달
- 게시글 본문은 HTML 문자열로 저장하고, 상세 화면에서는 `th:utext`로 렌더링
- 에디터가 생성한 HTML을 그대로 출력할 때 발생할 수 있는 XSS 위험을 줄이기 위해 저장 전 OWASP Java HTML Sanitizer 적용
- 확장자 검사와 Apache Tika 기반 MIME 타입 검사를 함께 적용하여 이미지 파일만 업로드 가능하도록 제한
- 업로드 직후의 이미지는 `post_id` 없이 임시 이미지로 저장
- 게시글 저장 / 수정 시 본문에 남아있는 이미지 id만 게시글과 매핑
- 이미 다른 게시글에 연결된 이미지는 재매핑하지 않도록 제한
- 게시글 삭제 시 해당 게시글에 매핑된 이미지 파일과 이미지 데이터를 함께 삭제
- 게시글에 매핑되지 않은 오래된 임시 이미지는 스케줄러로 정리

---

### 4. 게시글 조회 / 검색 / 조회수 동시성 처리

게시글 목록 조회에는 페이징과 검색 조건 유지를 적용하고, 조회수 증가는 DB update query로 처리했습니다.

- 제목+내용 / 제목 / 내용 / 작성자 기준 검색
- 검색어 앞뒤 공백 제거 및 2글자 미만 검색 제한
- 검색 결과 페이징 시 검색 조건 유지
- 검색 결과가 없을 경우 안내 메시지 표시
- 조회수는 JPQL update query로 `view_count = view_count + 1` 형태로 증가시켜 동시 요청에서 lost update를 방지

---

## 🔄 개선 예정

- 사용자가 작성 중인 임시 이미지를 다른 사용자가 매핑하지 못하도록 검증 추가
- 테스트 코드 재작성
- MySQL 전환
- AWS 배포

---

## 📌 프로젝트 목표

단순 CRUD 게시판 구현을 넘어서, 실제 백엔드 개발에서 필요한 인증/인가, 권한 검증, 예외 처리, 접근 제어, 데이터 상태 관리 등을 직접 설계하고 구현하는 것을 목표로 했습니다.
