package net.causw.application.dto.user;

import io.swagger.annotations.*;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.model.enums.UserState;
import java.util.List;
@Getter
@Setter
public class UserResponseDto {

    @ApiModelProperty(value = "고유 id값", example = "uuid 형식의 String 값입니다.")
    private String id;

    @ApiModelProperty(value = "이메일", example = "yebin@cau.ac.kr")
    private String email;

    @ApiModelProperty(value = "이름", example = "이예빈")
    private String name;

    @ApiModelProperty(value = "학번", example = "20209999")
    private String studentId;

    @ApiModelProperty(value = "입학년도", example = "2020")
    private Integer admissionYear;

    @ApiModelProperty(value = "역할", example = "COMMON")
    private Role role;

    @ApiModelProperty(value = "프로필 이미지 URL", example = "")
    private String profileImage;

    @ApiModelProperty(value = "상태", example = "AWAIT")
    private UserState state;

    @ApiModelProperty(value = "리더일 경우, 동아리 고유 id값", example = "['uuid 형식의 String 값입니다.', ...]")
    private List<String> circleIdIfLeader;

    @ApiModelProperty(value = "리더일 경우, 동아리 이름", example = "[개발 동아리, 퍼주마,..]")
    private List<String> circleNameIfLeader;


    private UserResponseDto(
            String id,
            String email,
            String name,
            String studentId,
            Integer admissionYear,
            Role role,
            String profileImage,
            UserState state,
            List<String> circleIdIfLeader,
            List<String> circleNameIfLeader
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.admissionYear = admissionYear;
        this.role = role;
        this.profileImage = profileImage;
        this.state = state;
        this.circleIdIfLeader = circleIdIfLeader;
        this.circleNameIfLeader = circleNameIfLeader;
    }

    public static UserResponseDto from(UserDomainModel user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getStudentId(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                user.getState(),
                null,
                null
        );
    }

    public static UserResponseDto from(
            UserDomainModel user,
            List<String> circleId,
            List<String> circleName
    ) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getStudentId(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                user.getState(),
                circleId,
                circleName
        );
    }
}
