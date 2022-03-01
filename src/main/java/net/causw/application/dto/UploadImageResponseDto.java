package net.causw.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadImageResponseDto {
    private String path;

    private UploadImageResponseDto(String path) {
        this.path = path;
    }

    public static UploadImageResponseDto of(String path) {
        return new UploadImageResponseDto(path);
    }
}
