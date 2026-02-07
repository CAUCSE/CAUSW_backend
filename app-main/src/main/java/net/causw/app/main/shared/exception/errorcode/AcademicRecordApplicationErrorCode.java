package net.causw.app.main.shared.exception.errorcode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.causw.app.main.shared.exception.BaseResponseCode;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum AcademicRecordApplicationErrorCode implements BaseResponseCode {
    ACADEMIC_RECORD_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "ACADEMIC_APPLICATION_404_001", "학적 변경 신청서를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public String getCode() {
        return "";
    }

    @Override
    public String getMessage() {
        return "";
    }

    @Override
    public HttpStatus getStatus() {
        return null;
    }
}
