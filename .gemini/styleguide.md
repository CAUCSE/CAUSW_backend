# CAUSW Alumni Network Code Review Guide

# Review Style

- Avoid general feedback, summaries, explanations of changes, or praises.
- Provide specific, objective insights only, without making broad comments on system impact or questioning intentions.
- Write all comments in Korean (ko-KR).

## **📌 Code Convention**

### **1. Naming Convention**

| Type               | Notation                                | Example                                |
|--------------------| --------------------------------------- | -------------------------------------- |
| Variables & Parameters & Methods | **camelCase**                           | `calendarService`, `findCalendar()`               |
| Classes & Interfaces & Enums | **PascalCase**                          | `CalendarController`, `BoardService`, `AcademicStatus`                |
| Constants                          | **UPPER_SNAKE_CASE**                    | `public static final String BOARD_NAME_APP_FREE = "자유";`               |
| Package Names                         | **lowercase with dots** | `net.causw.app.main.api`         |
| DTO Classes                   | **PascalCase with suffix**     | `CalendarResponseDto`, `BoardCreateRequestDto` |

### **2. Method Writing Principles**

- Each method should perform only one task
- If a method becomes too long (>30 lines), split it appropriately
- Use meaningful method names that describe the action clearly
- Private helper methods should be placed at the bottom of the class

**Method Naming Pattern**
- `find + Entity + By + Condition`: `findCalendarByYear()`, `findBoardById()`
- `create + Entity`: `createCalendar()`, `createBoard()`
- `update + Entity`: `updateCalendar()`, `updateBoard()`
- `delete + Entity`: `deleteCalendar()`, `deleteBoard()`

## **📌 CI Label Convention**

### **`db-change` 라벨**

PR에 다음 중 하나라도 해당하는 변경이 포함된 경우, `db-change` 라벨이 필요합니다.
라벨이 누락되어 있으면 리뷰 코멘트로 반드시 안내해야 합니다.

**라벨이 필요한 변경 사항:**
- Flyway 마이그레이션 스크립트 추가/수정 (`**/db/migration/**`)
- JPA Entity 클래스의 테이블/컬럼 구조 변경 (`@Entity`, `@Table`, `@Column` 등)
- `ddl-auto` 또는 DB 스키마에 영향을 주는 설정 변경

**리뷰 시 안내 예시:**
> 이 PR에는 DB 스키마 변경이 포함되어 있습니다. Flyway 마이그레이션 CI가 실행되려면 `db-change` 라벨을 추가해주세요.