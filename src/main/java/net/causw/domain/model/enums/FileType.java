package net.causw.domain.model.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FileType {
    USER_ADMISSION("USER_ADMISSION"), // TODO: 기존 코드와 충돌 나지 않도록 기존 String 값 유지. 추후 인프라와 함께 디렉토리 마이그레이션 및 리펙토링 필요.
    USER_ACADEMIC_RECORD_APPLICATION("user/academic-record-application");

    private final String directory;

}
