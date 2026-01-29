package net.causw.app.main.domain.campus.schedule.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScheduleType {
	ACADEMIC, // 학사일정
	DEPARTMENT, // 학부일정
	CCSSAA, // 크자회 일정
	STUDENT_COUNCIL, // 학생회 일정
	COMPETITION // 대회/공모전 일정
}
