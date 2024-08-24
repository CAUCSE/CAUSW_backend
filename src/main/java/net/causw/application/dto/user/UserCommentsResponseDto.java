package net.causw.application.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.comment.CommentsOfUserResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@Builder
public class UserCommentsResponseDto {
    private String id;
    private String email;
    private String name;
    private String studentId;
    private Integer admissionYear;
    private String  profileImage;
    private Page<CommentsOfUserResponseDto> comment;

    public static UserCommentsResponseDto of(
            User user,
            Page<CommentsOfUserResponseDto> comment
    ) {
        return UserCommentsResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .studentId(user.getStudentId())
                .admissionYear(user.getAdmissionYear())
                .profileImage(user.getProfileImage())
                .comment(comment)
                .build();
    }
}
