package net.causw.adapter.web;

import lombok.RequiredArgsConstructor;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordApplicationInfoResponseDto;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordInfoResponseDto;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordListResponseDto;
import net.causw.application.userAcademicRecord.UserAcademicRecordApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/academic-record")
@RequiredArgsConstructor
public class UserAcademicRecordApplicationController {

    private final UserAcademicRecordApplicationService userAcademicRecordApplicationService;

    public Page<UserAcademicRecordListResponseDto> getAllUserAcademicRecordPage() {
        return null;
    }

    public Page<UserAcademicRecordListResponseDto> getAllUserAwaitingAcademicRecordPage() {
        return null;
    }

    public Void requestAllUserAcademicRecordApplication() {
        return null;
    }

    public UserAcademicRecordInfoResponseDto getUserAcademicRecordInfo(String userId) {
        return null;
    }

    public UserAcademicRecordApplicationInfoResponseDto getUserAcademicRecordApplicationInfo(String userId) {
        return null;
    }


}
