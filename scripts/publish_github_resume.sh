#!/usr/bin/env bash
# publish_github.sh가 stage 1(chore/#1)에서 멈춘 뒤 이어서 실행하는 스크립트입니다.
# 먼저 아래 "1단계 마무리"를 실행한 다음, 이 스크립트로 2~10단계를 이어가세요.
#
# [1단계 마무리 — 터미널에 직접 입력]
#   git push origin chore/#1:main
#   gh issue close 1 --repo E-um-BSSM/E-um-BE-V2 \
#     --comment "초기 스캐폴딩이라 비교할 이전 커밋이 없어 PR 없이 main에 직접 반영했습니다 (chore/#1 = main 최초 커밋)."
#
# 그 다음 이 스크립트를 실행하세요: bash scripts/publish_github_resume.sh

set -euo pipefail

REPO="E-um-BSSM/E-um-BE-V2"

BRANCHES=(
  "feat/#2"
  "feat/#3"
  "feat/#4"
  "feat/#5"
  "feat/#6"
  "feat/#7"
  "feat/#8"
  "feat/#9"
  "docs/#10"
)

TITLES=(
  "feat: 공통 예외/응답/보안 인프라 구현"
  "feat: 엔티티 및 리포지토리 구현"
  "feat: 인증 API 구현"
  "feat: 클래스 API 구현"
  "feat: 멤버 API 구현"
  "feat: 지원서 폼 API 구현"
  "feat: 과제·제출 API 구현"
  "feat: 공지사항 API 구현"
  "docs: 아키텍처 문서 작성"
)

ISSUE_TITLES=(
  "[feat] 공통 예외/응답/보안 인프라 구현"
  "[feat] 엔티티 및 리포지토리 구현"
  "[feat] 인증 API 구현"
  "[feat] 클래스 API 구현"
  "[feat] 멤버 API 구현"
  "[feat] 지원서 폼 API 구현"
  "[feat] 과제·제출 API 구현"
  "[feat] 공지사항 API 구현"
  "[docs] 아키텍처 문서 작성"
)

ISSUE_BODIES=(
"## 작업 내용
공통 예외 처리, 응답 래퍼, JWT 기반 인증 인프라를 구현한다.

## 세부 작업
- [ ] ErrorCode / ApiException / GlobalExceptionHandler
- [ ] ErrorResponse / PageMetaResponse / PageResponse<T>
- [ ] JwtTokenProvider (액세스 토큰 발급/검증)
- [ ] JwtAuthenticationFilter / SecurityConfig / UserPrincipal / SecurityUtils
- [ ] CodeGenerator (인증코드/토큰/초대코드 생성)

## 참고
- EumMVPApi.json의 Error 스키마, bearerAuth 시큐리티 스킴"

"## 작업 내용
ERD와 API 명세를 바탕으로 JPA 엔티티와 Spring Data 리포지토리를 구현한다.

## 세부 작업
- [ ] User, EmailVerification, PasswordResetToken, RefreshToken
- [ ] Classroom, ClassroomMember(+ AccessScope/ClassStatus/Role/MemberStatus)
- [ ] ApplicationQuestion, ApplicationAnswer(+ QuestionType)
- [ ] Assignment, Submission(+ SubmissionStatus), Notice
- [ ] 각 엔티티에 대응하는 JpaRepository

## 참고
- erd.txt (waiting_list + classroom_members는 status 컬럼이 있는 단일 테이블로 병합)"

"## 작업 내용
회원가입/로그인/토큰재발급/로그아웃, 아이디·이메일 중복확인, 이메일 인증, 비밀번호 재설정 API를 구현한다.

## 세부 작업
- [ ] POST /auth/signup, /auth/signin, /auth/refresh, /auth/signout
- [ ] GET /auth/check-username, /auth/check-email
- [ ] POST /auth/email/send, /auth/email/verify
- [ ] POST /auth/password/reset-request, /auth/password/reset

## 참고
- EumMVPApi.json Auth 태그"

"## 작업 내용
클래스 개설/검색/내 클래스 목록/상세/수정/삭제 API를 구현한다.

## 세부 작업
- [ ] POST /classes, GET /classes/search, GET /classes/my
- [ ] GET/PATCH/DELETE /classes/{classId}
- [ ] 인기순(POPULAR)/최신순(RECENT) 정렬
- [ ] mentee_count, invite_code(멘토 전용), my_role 계산

## 참고
- EumMVPApi.json Classroom 태그"

"## 작업 내용
초대코드 발급/조회, 가입신청/취소, 대기목록·멤버목록 조회, 가입 수락, 강퇴/거절 API를 구현한다.

## 세부 작업
- [ ] POST/GET /classes/{classId}/invite
- [ ] POST/DELETE /classes/{classId}/join
- [ ] GET /classes/{classId}/waiting, /classes/{classId}/members
- [ ] PATCH /classes/{classId}/members/{userId}/accept
- [ ] DELETE /classes/{classId}/members/{userId}

## 참고
- EumMVPApi.json Member 태그"

"## 작업 내용
클래스 지원서 폼 조회/설정(전체 교체) API를 구현한다.

## 세부 작업
- [ ] GET /classes/{classId}/application-form
- [ ] PUT /classes/{classId}/application-form
- [ ] SHORT_TEXT/SINGLE_CHOICE/LONG_TEXT 질문 유형별 검증

## 참고
- EumMVPApi.json Application 태그"

"## 작업 내용
과제 생성/목록/상세/수정/삭제 및 제출/제출취소/제출목록·상세/채점 API를 구현한다.

## 세부 작업
- [ ] POST/GET /classes/{classId}/assignments, /{assignmentId}
- [ ] PATCH/DELETE /classes/{classId}/assignments/{assignmentId}
- [ ] POST/GET/DELETE .../submissions, /{submissionId}
- [ ] PATCH .../submissions/{submissionId}/feedback

## 참고
- EumMVPApi.json Assignment/Submission 태그"

"## 작업 내용
클래스 공지사항 생성/목록/상세/수정/삭제 API를 구현한다.

## 세부 작업
- [ ] POST/GET /classes/{classId}/notices
- [ ] GET/PATCH/DELETE /classes/{classId}/notices/{noticeId}

## 참고
- EumMVPApi.json Notice 태그"

"## 작업 내용
구현된 아키텍처(도메인 구조, ERD와의 차이점, 레이어 구조)를 CLAUDE.md에 문서화한다.

## 세부 작업
- [ ] 프로젝트/커맨드/스택 섹션 정리
- [ ] ERD와 다르게 구현한 부분 기록 (사용자 ID 타입, waiting_list 병합 등)
- [ ] 패키지 구조/레이어 흐름 문서화"
)

for i in "${!BRANCHES[@]}"; do
  branch="${BRANCHES[$i]}"
  title="${TITLES[$i]}"
  issue_title="${ISSUE_TITLES[$i]}"
  issue_body="${ISSUE_BODIES[$i]}"

  echo "=== [$branch] 이슈 생성 ==="
  issue_url=$(gh issue create --repo "$REPO" --title "$issue_title" --body "$issue_body")
  issue_number=$(basename "$issue_url")
  echo "issue #$issue_number created: $issue_url"

  echo "=== [$branch] 브랜치 push ==="
  git push -u origin "$branch"

  echo "=== [$branch] PR 생성 ==="
  pr_body="## 관련 이슈
Closes #$issue_number

## 변경 사항
- ${title#*: }

## 테스트
- ./gradlew test (로컬 확인 필요)"
  pr_url=$(gh pr create --repo "$REPO" --base main --head "$branch" --title "$title" --body "$pr_body")
  echo "PR created: $pr_url"

  echo "=== [$branch] PR 머지 ==="
  gh pr merge "$pr_url" --merge --delete-branch=false

  echo "=== [$branch] 완료 ==="
done

echo "모든 단계 완료. GitHub 저장소를 확인해보세요: https://github.com/$REPO"
