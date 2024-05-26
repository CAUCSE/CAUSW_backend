package net.causw.application.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.user.User;
import org.springframework.data.domain.Page;

@Getter
@Setter
@Builder
public class UserPostsResponseDto {
    private String id;
    private String email;
    private String name;
    private String studentId;
    private Integer admissionYear;
    private String profileImage;
    private Page<UserPostResponseDto> post;

    public static UserPostsResponseDto of(
            User user,
            Page<UserPostResponseDto> post
    ) {
        return UserPostsResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .studentId(user.getStudentId())
                .admissionYear(user.getAdmissionYear())
                .profileImage(user.getProfileImage())
                .post(post)
                .build();
    }
}