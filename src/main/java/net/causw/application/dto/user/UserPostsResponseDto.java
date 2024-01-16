package net.causw.application.dto.user;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.user.UserDomainModel;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class UserPostsResponseDto {
    private String id;
    private String email;
    private String name;
    private String studentId;
    private Integer admissionYear;
    private String profileImage;
    private Page<UserPostResponseDto> post;

    private UserPostsResponseDto(
            String id,
            String email,
            String name,
            String studentId,
            Integer admissionYear,
            String profileImage,
            Page<UserPostResponseDto> post
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.admissionYear = admissionYear;
        this.profileImage = profileImage;
        this.post = post;
    }

    public static UserPostsResponseDto from(
            UserDomainModel user,
            Page<UserPostResponseDto> post
    ) {
        return new UserPostsResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getStudentId(),
                user.getAdmissionYear(),
                user.getProfileImage(),
                post
        );
    }
}