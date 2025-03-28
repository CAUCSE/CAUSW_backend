package net.causw.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import net.causw.application.dto.notification.NotificationResponseDto;
import net.causw.application.notification.NotificationLogService;
import net.causw.application.notification.NotificationService;
import net.causw.config.security.userdetails.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications/log")
public class NotificationController {
    private final NotificationLogService notificationLogService;


    @GetMapping("/general")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "유저에게 온 일반 알람 조회", description = "유저의 일반 알림을 조회합니다.")
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    public List<NotificationResponseDto> findGeneralNotification(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return notificationLogService.getGeneralNotification(userDetails.getUser());
    }

    @GetMapping("/ceremony")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "유저에게 온 경조사 알람 조회", description = "유저의 경조사 알람을 조회합니다.")
    public List<NotificationResponseDto> getCeremonyNotification(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return notificationLogService.getCeremonyNotification(userDetails.getUser());
    }

    @PostMapping("/isRead/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "유저에게 온 알람 읽음 여부 변경",
            description = "유저의 알람 조회 여부를 참으로 변경합니다<br> " +
            "id에는 notification_log id를 넣어주세요")
    public void readNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") String id
    ){
        notificationLogService.readNotification(userDetails.getUser(), id);
    }




}
