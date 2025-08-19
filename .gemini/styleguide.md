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
| Package Names                         | **lowercase with dots** | `net.causw.app.main.controller`         |
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