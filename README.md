# ✅ 프로젝트 소개
- Spring Boot 기반의 게시판 프로젝트입니다.
- 백엔드 개발에 초점을 맞춰 기본 기능을 구현했습니다.
- 성능 개선과 데이터 정합성, 유지보수성을 고려한 설계 및 구현을 목표로 개발했습니다.

# ✅ 사용 기술 및 개발 환경
Spring Boot, Gradle, Java17, IntelliJ, JWT, MySQL, Redis, Docker, AWS EC2

# ✅ 시스템 아키텍처
<img width="641" height="395" alt="board_서버구조도" src="https://github.com/user-attachments/assets/332d6cd5-9b00-42cd-ae31-a3e601386f98" />

# ✅ ERD
<img width="865" height="675" alt="board_erd" src="https://github.com/user-attachments/assets/4cb4bc7c-7c27-45c1-85a1-42fa5efa73d2" />

# ✅ 주요 기능
1. 회원가입
2. 로그인 / 로그아웃
3. 게시글 작성 / 수정 / 삭제 / 조회
4. 댓글 작성 / 삭제
5. 좋아요 / 좋아요 해제
6. 좋아요 및 댓글 알림 - 다른 사람이 활동한 경우

# ✅ 성능 개선
- Redis INCR를 활용하여 조회수 DB 쓰기 병목 완화
- Scheduler를 통한 Redis 조회수와 DB 간 데이터 동기화
- Redis KEYS 명령을 SCAN 기반으로 개선하여 블로킹 이슈 완화
- WebSocket(STOMP) 기반 실시간 알림 및 비동기 처리
- Apache Bench(ab)와 k6를 활용한 부하 테스트 및 병목 분석
