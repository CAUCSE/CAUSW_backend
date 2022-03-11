package net.causw.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadFileResponseDto {
    private String path;

    private UploadFileResponseDto(String path) {
        this.path = path;
    }

    public static UploadFileResponseDto of(String path) {
        return new UploadFileResponseDto(path);
    }
}
