package net.causw.application.dto.user;

import io.swagger.annotations.*;
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

    @ApiModelProperty(value = "이메일", example = "yebin@cau.ac.kr")
    private String email;

    @ApiModelProperty(value = "자기소개 글 (255자 이내)", example = "안녕하세요! 코딩을 좋아하는 신입생 이예빈입니다.")
    private String description;

    @ApiModelProperty(value = "이미지", example = "")
    private MultipartFile attachImage;

    public Optional<MultipartFile> getAttachImage() {
        return Optional.ofNullable(this.attachImage);
    }
}
