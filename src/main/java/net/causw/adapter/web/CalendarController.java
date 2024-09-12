package net.causw.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.application.calendar.CalendarService;
import net.causw.application.dto.calendar.CalendarCreateRequestDto;
import net.causw.application.dto.calendar.CalendarResponseDto;
import net.causw.application.dto.calendar.CalendarUpdateRequestDto;
import net.causw.application.dto.calendar.CalendarsResponseDto;
import net.causw.domain.exceptions.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calendars")
public class CalendarController {
    private final CalendarService calendarService;

    @GetMapping
    @Operation(summary = "캘린더 조회 API", description = "캘린더 조회 API 입니다.")
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and @securityService.adminOrPresidentOrVicePresident")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "캘린더를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "User id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public CalendarsResponseDto findCalendars(@RequestParam("year") Integer year) {
        return calendarService.findCalendarByYear(year);
    }

    @GetMapping("/{calendarId}")
    @Operation(summary = "캘린더 단일 조회 API", description = "캘린더 단일 조회 API 입니다.")
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and @securityService.adminOrPresidentOrVicePresident")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "캘린더를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "User id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public CalendarResponseDto findCalendars(@PathVariable("calendarId") String calendarId) {
        return calendarService.findCalendar(calendarId);
    }

    @GetMapping("/home")
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "홈페이지 캘린더 조회 API", description = "홈페이지 캘린더 조회 API 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "캘린더를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "User id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public CalendarResponseDto findHomeCalendar() {
        return calendarService.findCalendar();
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(summary = "캘린더 생성 API", description = "캘린더 생성 API 입니다.")
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and @securityService.adminOrPresidentOrVicePresident")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "CREATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4001", description = "이미 존재하는 캘린더 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "User id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public CalendarResponseDto createCalendar(
            @Valid @ModelAttribute CalendarCreateRequestDto calendarCreateRequestDto) {
        return calendarService.createCalendar(calendarCreateRequestDto);
    }

    @PutMapping("/{calendarId}")
    @Operation(summary = "캘린더 수정 API", description = "캘린더 수정 API 입니다.")
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and @securityService.adminOrPresidentOrVicePresident")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "CREATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "캘린더를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "User id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public CalendarResponseDto updateCalendar(
            @PathVariable("calendarId") String calendarId,
            @Valid @ModelAttribute CalendarUpdateRequestDto calendarUpdateRequestDto) {
        return calendarService.updateCalendar(calendarId, calendarUpdateRequestDto);
    }
}
