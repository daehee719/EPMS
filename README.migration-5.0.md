# Migration 5.0 Plan

이 문서는 `migration-5.0` 브랜치에서 진행하는 eGovFrame 5.0(beta) 마이그레이션 계획입니다.

## 목표 기준
- Spring Boot 3.5.6
- Spring Framework 6.2.11
- Spring Security 6.5.5
- JDK 17
- Servlet 6 / Jakarta EE 10

## 진행 원칙
- 한 단계씩 컴파일/런타임 검증
- 변경 범위는 작게, 영향도는 기록
- JSP는 초기 단계에서 유지

## Task / SubTask

### 1) 빌드/의존성 전환
- 1.1 `egovframe-boot-starter-parent:5.0.0` 적용
- 1.2 `java.version=17` 적용
- 1.3 eGov RTE 의존성 5.0 라인으로 정리
- 1.4 `javax.*` → `jakarta.*` 의존성 교체
- 1.5 Spring 버전 선언 제거(Parent 상속)

### 2) 코드 패키지 전환 (javax → jakarta)
- 2.1 `javax.servlet.*` → `jakarta.servlet.*`
- 2.2 `javax.annotation.*` → `jakarta.annotation.*`
- 2.3 `javax.inject.*` → `jakarta.inject.*`
- 2.4 `javax.validation.*` → `jakarta.validation.*`
- 2.5 `javax.websocket.*` → `jakarta.websocket.*` (유지 시)
- 2.6 외부 라이브러리 변환 (원패스/GPki: javax → jakarta 변환)

### 3) 설정 전환 (Boot 3)
- 3.1 XML 설정 분류 및 Java Config/YAML 전환 계획 수립
- 3.2 보안 설정 Spring Security 6.5.5 호환
- 3.3 DataSource/Transaction/MyBatis 설정 재정의

### 4) 기능 단위 검증
- 4.1 로그인/권한 흐름 검증
- 4.2 프로그램 관리 CRUD/업로드/다운로드 검증
- 4.3 공통코드/메뉴/권한 조회 검증

### 5) JSP/JSTL 유지 검증
- 5.1 JSP 렌더링
- 5.2 파일 업/다운로드 응답
- 5.3 에러 페이지 핸들링

### 6) 배포/운영 전환
- 6.1 WAR/JAR 정책 확정
- 6.2 Jenkins 파이프라인(JDK17) 반영
- 6.3 롤백 플랜 작성

## 진행 로그
- 시작 브랜치: `migration-5.0`
- 원패스/GPki 변환 프로필 추가: `mvn -P transform-jakarta -DskipTests generate-sources`
