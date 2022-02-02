package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.UserDomainModel;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor
public class UserCommentResponseDto {
    private String id;
    private String email;
    private String name;
    private String studentId;
    private Integer admissionYear;
    private String profileImage;
    private Page<CommentAllForUserResponseDto> comment;

    private UserCommentResponseDto(
            String id,
            String email,
            String name,
            String studentId,
            Integer admissionYear,
            String profileImage,
            Page<CommentAllForUserResponseDto> comment
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.admissionYear = admissionYear;
        this.profileImage = profileImage;
        this.comment = comment;
    }

    public static UserCommentResponseDto from(
            UserDomainModel user,
            Page<CommentAllForUserResponseDto> comment
    ) {
        return new UserCommentResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getStudentId(),
                user.getAdmissionYear(),
                user.getProfileImage(),
                comment
        );
    }
}
