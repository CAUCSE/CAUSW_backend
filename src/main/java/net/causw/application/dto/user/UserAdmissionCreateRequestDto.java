package net.causw.application.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAdmissionCreateRequestDto {
    private String email;
    private String description;
    private MultipartFile attachImage;

    public Optional<MultipartFile> getAttachImage() {
        return Optional.ofNullable(this.attachImage);
    }
}
