package net.causw.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import net.causw.application.dto.semester.CreateSemesterRequestDto;
import net.causw.application.dto.semester.CurrentSemesterResponseDto;
import net.causw.application.semester.SemesterService;
import net.causw.config.security.userdetails.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping("/current")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "현재 학기 조회(개발 테스트 및 관리자용)", description = "현재 진행 중인 학기를 조회합니다.")
    public CurrentSemesterResponseDto getCurrentSemester() {
        return semesterService.getCurrentSemester();
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "학기 목록 조회(개발 테스트 및 관리자용)", description = "모든 학기 목록을 조회합니다.")
    public List<CurrentSemesterResponseDto> getSemesterList() {
        return semesterService.getSemesterList();
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "학기 생성(개발 테스트 및 관리자용)", description = "새로운 학기를 생성합니다.")
    public Void createSemester(
            @RequestBody CreateSemesterRequestDto createSemesterRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return semesterService.createSemester(createSemesterRequestDto, userDetails.getUser());
    }

    @DeleteMapping("/{semesterId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "학기 삭제(개발 테스트 및 관리자용)", description = "특정 학기를 삭제합니다.")
    public Void deleteSemester(
            @PathVariable(value = "semesterId") String semesterId
    ) {
        return semesterService.deleteSemester(semesterId);
    }



}
