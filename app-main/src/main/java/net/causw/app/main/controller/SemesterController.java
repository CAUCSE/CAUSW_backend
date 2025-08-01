package net.causw.app.main.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.dto.semester.CreateSemesterRequestDto;
import net.causw.app.main.dto.semester.CurrentSemesterResponseDto;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.app.main.service.semester.SemesterService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/semesters")
@RequiredArgsConstructor
public class SemesterController {

	private final SemesterService semesterService;

	@GetMapping("/current")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("@security.hasRole(@Role.ADMIN)")
	@Operation(summary = "현재 학기 조회(개발 테스트 및 관리자용)", description = "현재 진행 중인 학기를 조회합니다.")
	public CurrentSemesterResponseDto getCurrentSemester() {
		return semesterService.getCurrentSemester();
	}

	@GetMapping("/list")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("@security.hasRole(@Role.ADMIN)")
	@Operation(summary = "학기 목록 조회(개발 테스트 및 관리자용)", description = "모든 학기 목록을 조회합니다.")
	public List<CurrentSemesterResponseDto> getSemesterList() {
		return semesterService.getSemesterList();
	}

	@PostMapping("/create")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@security.hasRole(@Role.ADMIN)")
	@Operation(summary = "학기 생성(개발 테스트 및 관리자용)", description = "새로운 학기를 생성합니다.")
	public void createSemester(
		@RequestBody CreateSemesterRequestDto createSemesterRequestDto,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		semesterService.createSemester(createSemesterRequestDto, userDetails.getUser());
	}

	/**
	 * 다음 학기 생성(재학 인증 일괄 요청)
	 * @param userDetails
	 */
	@PostMapping("/create/next")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@security.hasRoleGroup(RoleGroup.EXECUTIVES)")
	@Operation(summary = "다음 학기 생성(재학 인증 일괄 요청)", description = "다음 학기를 생성합니다. 자동으로 재학 인증도 일괄 요청 됩니다.")
	public void createNextSemester(
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		semesterService.createNextSemester(userDetails.getUser());
	}

	@DeleteMapping("/{semesterId}")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("@security.hasRole(@Role.ADMIN)")
	@Operation(summary = "학기 삭제(개발 테스트 및 관리자용)", description = "특정 학기를 삭제합니다.")
	public void deleteSemester(
		@PathVariable(value = "semesterId") String semesterId
	) {
		semesterService.deleteSemester(semesterId);
	}
}
