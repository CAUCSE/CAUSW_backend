package net.causw.application.dto;

//import com.google.cloud.storage.Blob;
import lombok.Getter;
import lombok.Setter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Getter
@Setter
public class FileResponseDto {
    private String originalFileName;
    private String downloadFilePath;

    private FileResponseDto(
            String originalFileName,
            String downloadFilePath
    ) {
        this.originalFileName = originalFileName;
        this.downloadFilePath = downloadFilePath;
    }

    public static FileResponseDto from(String filePath) {
        return new FileResponseDto(
                Arrays.stream(URLDecoder.decode(filePath, StandardCharsets.UTF_8).split("/"))
                        .reduce((a, b) -> b).orElse(null),
                filePath
        );
    }
}
