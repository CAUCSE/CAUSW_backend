package net.causw.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.enums.UserState;
import net.causw.domain.model.user.UserDomainModel;

import java.util.List;

@Getter
@Setter
public class UserResponseDto {

    @Schema(description = "고유 id값", example = "uuid 형식의 String 값입니다.")
    private String id;

    @Schema(description = "이메일", example = "yebin@cau.ac.kr")
    private String email;

    @Schema(description = "이름", example = "이예빈")
    private String name;

    @Schema(description = "학번", example = "20209999")
    private String studentId;

    @Schema(description = "입학년도", example = "2020")
    private Integer admissionYear;

    @Schema(description = "역할", example = "COMMON")
    private Role role;

    @Schema(description = "프로필 이미지 URL", example = "")
    private String profileImage;

    @Schema(description = "상태", example = "AWAIT")
    private UserState state;

    @Schema(description = "리더일 경우, 동아리 고유 id값 리스트", example = "['uuid 형식의 String 값입니다.', ...]")
    private List<String> circleIdIfLeader;

    @Schema(description = "리더일 경우, 동아리 이름 리스트", example = "[개발 동아리, 퍼주마,..]")
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
