package net.causw.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.util.CircleServiceDtoMapper;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.model.enums.UserState;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
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

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .studentId(user.getStudentId())
                .admissionYear(user.getAdmissionYear())
                .role(user.getRole())
                .profileImage(user.getProfileImage())
                .state(user.getState())
                .build();
    }

    //Circle Service 단에서 domainmodel 이 삭제되지 않아 충돌 방지를 위해 별도 생성
    public static UserResponseDto from(UserDomainModel user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .studentId(user.getStudentId())
                .admissionYear(user.getAdmissionYear())
                .role(user.getRole())
                .profileImage(user.getProfileImage())
                .state(user.getState())
                .build();
    }

    public static UserResponseDto of(
            User user,
            List<String> circleId,
            List<String> circleName
    ) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .studentId(user.getStudentId())
                .admissionYear(user.getAdmissionYear())
                .role(user.getRole())
                .profileImage(user.getProfileImage())
                .state(user.getState())
                .circleIdIfLeader(circleId)
                .circleNameIfLeader(circleName)
                .build();
    }

}
