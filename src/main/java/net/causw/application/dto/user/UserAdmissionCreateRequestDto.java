package net.causw.application.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Data
@AllArgsConstructor
public class UserAdmissionCreateRequestDto {
    private String email;
    private String description;
    private MultipartFile attachImage;

    public Optional<MultipartFile> getAttachImage() {
        return Optional.ofNullable(attachImage);
    }
}
