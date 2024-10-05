package net.causw.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import net.causw.application.dto.notification.NotificationResponseDto;
import net.causw.application.notification.NotificationService;
import net.causw.config.security.userdetails.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "알림 조회", description = "알림을 조회합니다.")
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    public List<NotificationResponseDto> findNotice(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return notificationService.findUserNotice(userDetails.getUser());
    }

    @PutMapping("/{boardId}")
    @Operation(summary = "알림 설정 변경", description = "알림 설정을 변경합니다.")
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    public void toggleNotice(
            @PathVariable("boardId") String boardId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.setNotice(userDetails.getUser(), boardId);
    }

    @GetMapping("/{boardId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "알림설정 여부 확인", description = "알림설정 여부를 확인합니다.")
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    public Boolean checkNotice(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable("boardId") String boardId) {
        return notificationService.checkNotice(userDetails.getUser(), boardId);
    }
}
