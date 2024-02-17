package net.causw.application.dto.user;

import lombok.Getter;
import lombok.Setter;
import net.causw.application.dto.comment.CommentsOfUserResponseDto;
import net.causw.domain.model.user.UserDomainModel;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class UserCommentsResponseDto {
    private String id;
    private String email;
    private String name;
    private String studentId;
    private Integer admissionYear;
    private String profileImage;
    private Page<CommentsOfUserResponseDto> comment;

    private UserCommentsResponseDto(
            String id,
            String email,
            String name,
            String studentId,
            Integer admissionYear,
            String profileImage,
            Page<CommentsOfUserResponseDto> comment
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.admissionYear = admissionYear;
        this.profileImage = profileImage;
        this.comment = comment;
    }

    public static UserCommentsResponseDto from(
            UserDomainModel user,
            Page<CommentsOfUserResponseDto> comment
    ) {
        return new UserCommentsResponseDto(
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
