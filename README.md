# 📝 Spring Board Project

Spring Boot와 Thymeleaf를 사용해 구현한 게시판 프로젝트입니다.  
회원/비회원 작성자를 구분하고, 게시글·댓글 권한 검증, 관리자 기능, 회원탈퇴까지 포함한 기본 커뮤니티 구조를 구현했습니다.

---

## 🚀 주요 기능

- 회원가입 / 로그인 / 로그아웃
- 게시글 CRUD 및 조회수 증가
- 게시글 키워드 검색
- Toast UI Editor 기반 게시글 작성 / 수정
- 에디터 본문 이미지 업로드
- 댓글 작성 / 삭제
- 회원 / 비회원 작성자 구분
- 관리자 기능
    - 회원 검색
    - 회원 상세 조회
    - 관리자 권한 부여 / 해제
- 마이페이지
    - 내 정보 조회
    - 내가 작성한 글 / 댓글 조회
    - 회원탈퇴
- 전역 예외 처리

---

## 🏗 기술 스택

- Java
- Spring Boot
- Spring Data JPA
- Thymeleaf
- H2 Database
- Gradle
- 
## 📚 사용 라이브러리

- Toast UI Editor
- OWASP Java HTML Sanitizer
- Apache Tika

---

## 🔧 구현 포인트

### 1. 회원 / 비회원 / 관리자 권한 분기

게시글과 댓글의 작성 주체에 따라 권한 검증 방식을 분리했습니다.

- 회원 작성 글/댓글은 로그인 회원의 id와 작성자의 member_id를 비교
- 비회원 작성 글/댓글은 작성 시 입력한 비밀번호를 BCrypt로 해시하여 저장하고, 수정/삭제 시 검증
- 관리자는 일반 사용자의 글/댓글을 관리할 수 있도록 별도 권한 분기 처리

---

### 2. 수정 요청 이중 검증

게시글 수정 시 단순히 수정 페이지 진입에서만 권한을 검사하지 않고, 실제 수정 요청에서도 다시 권한을 검증하도록 구성했습니다.

- 수정 페이지 진입 시 권한 검증
- 실제 수정 요청 시 권한 재검증
- 비회원 글 수정의 경우 비밀번호 검증 후 세션에 검증 정보를 저장하여 처리

---

### 3. 세션 기반 로그인 관리

로그인 성공 시 필요한 회원 정보를 `SessionMember`에 담아 세션에 저장하고, 컨트롤러와 서비스 계층에서 로그인 상태와 권한 검증에 활용했습니다.

---

### 4. Interceptor를 이용한 접근 제어

반복되는 접근 권한 검사를 줄이기 위해 Interceptor를 사용했습니다.

- 관리자 페이지는 관리자 권한이 있는 사용자만 접근 가능
- 마이페이지와 회원탈퇴 페이지는 로그인한 사용자만 접근 가능

---

### 5. Enum을 이용한 권한 / 상태 관리

문자열 기반으로 관리하던 회원 권한과 상태를 Enum으로 변경하여 오타 가능성을 줄이고 타입 안정성을 높였습니다.

- `Role` : USER, ADMIN
- `Status` : ACTIVE, WITHDRAWN

---

### 6. 회원탈퇴 처리

회원탈퇴 시 회원 데이터를 물리적으로 삭제하지 않고, 회원 상태를 `WITHDRAWN`으로 변경하는 소프트 삭제 방식을 적용했습니다.

이를 통해 기존 게시글과 댓글의 작성자 관계를 유지하면서도, 탈퇴한 회원은 더 이상 로그인할 수 없도록 처리했습니다.

---

### 7. 전역 예외 처리

`@ControllerAdvice`를 사용해 공통 예외 처리 구조를 분리했습니다.  
서비스 계층에서 발생한 예외를 공통 에러 페이지로 전달하여 컨트롤러의 예외 처리 부담을 줄였습니다.

---

### 8. 조회수 증가 동시성 처리

게시글 조회수 증가 시 `조회 -> 값 증가 -> 저장` 방식에서 발생할 수 있는 lost update 문제를 막기 위해 JPQL update query를 사용했습니다.

```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("update Post p set p.viewCount = p.viewCount + 1 where p.id = :id")
int increaseViewCount(@Param("id") Long id);
```

조회수를 자바에서 계산하지 않고 DB에서 `view_count = view_count + 1` 형태로 증가시켜 동시 요청 상황에서도 조회수 증가가 누락되지 않도록 처리했습니다.

---

### 9. RESTful URL 구조 정리

게시글과 댓글을 리소스 중심 URL로 정리했습니다.

| 기능 | Method | URL |
|---|---|---|
| 게시글 작성 페이지 | GET | `/posts/new` |
| 게시글 작성 | POST | `/posts` |
| 게시글 조회 | GET | `/posts/{id}` |
| 게시글 수정 페이지 요청 | POST | `/posts/{id}/edit` |
| 게시글 수정 | PATCH | `/posts/{id}` |
| 게시글 삭제 | DELETE | `/posts/{id}` |
| 댓글 작성 | POST | `/posts/{id}/comments` |
| 댓글 삭제 | DELETE | `/posts/{postId}/comments/{commentId}` |

비회원 게시글 수정 페이지 접근 시 비밀번호 검증이 필요하므로, 수정 폼 요청은 URL에 비밀번호가 노출되지 않도록 POST로 처리했습니다. 실제 게시글 수정과 삭제 요청은 HTML form의 `_method` hidden field를 사용해 PATCH, DELETE로 매핑했습니다.

---

### 10. Toast UI Editor 기반 게시글 작성 / 수정

기존 textarea 기반 게시글 작성 / 수정 화면을 Toast UI Editor 기반 웹 에디터로 변경했습니다.

- 작성 / 수정 화면에서 Toast UI Editor를 초기화하고, 제출 시 `editor.getHTML()` 결과를 `PostDto.content`로 전달
- 게시글 본문은 HTML 문자열로 저장하고, 상세 화면에서는 `th:utext`로 렌더링
- 기존 대표 이미지 업로드 방식 대신 에디터 본문 안에 이미지를 삽입하는 방식으로 변경
- 에디터가 생성한 HTML을 그대로 출력할 때 발생할 수 있는 XSS 위험을 줄이기 위해 저장 전 OWASP Java HTML Sanitizer 적용

```java
PolicyFactory policy = Sanitizers.FORMATTING
        .and(Sanitizers.BLOCKS)
        .and(Sanitizers.LINKS)
        .and(Sanitizers.IMAGES);

post.setContent(policy.sanitize(postDto.getContent()));
```

---

### 11. 에디터 본문 이미지 업로드

Toast UI Editor의 이미지 업로드 hook을 사용해 본문에 이미지를 붙여넣거나 업로드할 때 서버에 즉시 저장되도록 구현했습니다.

- 에디터의 `addImageBlobHook`에서 이미지 파일을 `FormData`로 감싸 `/editor/images`로 전송
- 컨트롤러에서 `@RequestParam("imageFile") MultipartFile imageFile`로 이미지 파일 수신
- `ImageService`에서 UUID 기반 파일명으로 저장하고 `/images/post/{파일명}` 형태의 URL 반환
- 반환된 URL을 에디터 callback에 전달하여 본문 HTML에 `<img src="...">` 형태로 삽입
- 확장자 검사와 Apache Tika 기반 MIME 타입 검사를 함께 적용하여 이미지 파일만 업로드 가능하도록 제한
- `WebConfig`에서 `/images/post/**` 요청을 업로드 디렉터리로 매핑하여 저장된 이미지를 브라우저에서 조회 가능하도록 처리

---

### 12. 게시글 검색

게시글 목록에서 검색 조건과 키워드를 이용해 게시글을 조회할 수 있도록 구현했습니다.

- 제목+내용 / 제목 / 내용 / 작성자 기준 검색
- 검색어 앞뒤 공백 제거 및 2글자 미만 검색 제한
- 검색 결과 페이징 시 검색 조건 유지
- 검색 결과가 없을 경우 안내 메시지 표시

---

### 13. 테스트

- 게시글 삭제 권한 검증 테스트
- 조회수 동시성 테스트

조회수 동시성 테스트에서는 100개 스레드로 총 100,000번 조회수 증가 요청을 실행하고, 최종 조회수가 정확히 100,000인지 검증했습니다.

---

## 🔄 개선 예정

- 게시글 삭제 시 본문에 포함된 이미지 파일 함께 삭제
- 에디터 본문에서 제거된 이미지 파일 정리
- 게시글 작성 / 수정 시 제목, 작성자, 내용 빈 값 검증
- 게시글 수정 페이지 진입 시 기존 제목 / 내용 / 작성자 기본값 표시
- MySQL 전환
- AWS 배포
- Spring Security 기반 인증 적용

---

## 📌 프로젝트 목표

단순 CRUD 게시판 구현을 넘어서, 실제 백엔드 개발에서 필요한 권한 검증, 세션 관리, 예외 처리, 접근 제어, 데이터 상태 관리 등을 직접 설계하고 구현하는 것을 목표로 했습니다.
