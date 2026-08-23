package net.causw.app.main.core.aop.enums;

public enum AdminTarget {
	SYSTEM_ONLY, // 시스템 관리자 전용 (SYSTEM_ADMIN)
	ALL_ADMIN, // 모든 관리자 (SYSTEM_ADMIN, 모든 ADMIN)
	ENROLLED_ADMIN, // 재학생 관리자 (SYSTEM_ADMIN 또는 AcademicStatus가 ENROLLED인 ADMIN)
	GRADUATED_ADMIN // 졸업생 관리자 (SYSTEM_ADMIN 또는 AcademicStatus가 GRADUATED인 ADMIN)
}
