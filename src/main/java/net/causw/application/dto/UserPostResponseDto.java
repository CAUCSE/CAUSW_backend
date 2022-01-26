package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.domain.model.UserDomainModel;
import org.springframework.data.domain.Page;

@Getter
@Setter
@NoArgsConstructor
public class UserPostResponseDto {
    private String id;
    private String email;
    private String name;
    private String studentId;
    private Integer admissionYear;
    private String profileImage;
    private Page<PostAllForUserResponseDto> post;

    private UserPostResponseDto(
            String id,
            String email,
            String name,
            String studentId,
            Integer admissionYear,
            String profileImage,
            Page<PostAllForUserResponseDto> post
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.admissionYear = admissionYear;
        this.profileImage = profileImage;
        this.post = post;
    }

    public static UserPostResponseDto from(
            UserDomainModel user,
            Page<PostAllForUserResponseDto> post
    ) {
        return new UserPostResponseDto(
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