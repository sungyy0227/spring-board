# 📝 Spring Board Project

Spring Boot로 구현한 게시판 프로젝트입니다.  
회원/비회원 글 작성, 댓글 기능, 관리자 기능까지 포함한 커뮤니티 구조를 구현했습니다.

---

## 🚀 주요 기능

- 회원가입 / 로그인 (세션 기반)
- 게시글 CRUD (작성, 조회, 수정, 삭제)
- 댓글 작성 / 삭제
- 회원 / 비회원 글 작성 구분
- 관리자 기능 (회원 조회, 권한 부여/해제)
- 전역 예외 처리 (`@ControllerAdvice`)를 적용하여 공통 에러 처리 구조 구현

---

## 🏗 기술 스택

- Spring Boot
- Spring Data JPA
- Thymeleaf
- H2 Database

---

## 🔧 구현 포인트

- 회원 / 비회원 / 관리자에 따른 게시글 권한 분기 처리
- 비회원 글의 경우 비밀번호 기반으로 수정/삭제 권한 검증 로직 구현
- 수정 페이지 진입과 실제 수정 요청을 분리하여 이중 권한 검증 구조 설계
- SessionMember를 활용한 세션 기반 로그인 상태 관리
- Interceptor를 이용한 관리자 페이지 접근 제어
- 전역 예외 처리(`@ControllerAdvice`)를 통해 공통 에러 처리 로직 분리

---

## 🔄 개선 예정

- RESTful 구조로 리팩토링 (POST → DELETE / PATCH)
- role enum으로 변경

---

## 📌 목표

- REST API 구조로 확장
- Spring Security / JWT 적용
- AWS 배포
