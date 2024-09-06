package net.causw.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.post.PostsResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

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

    @Schema(description = "간락화된 게시글 정보들입니다")
    private Page<PostsResponseDto> posts;


    public static UserPostsResponseDto of(
            User user,
            Page<PostsResponseDto> post
    ) {
        return UserPostsResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .studentId(user.getStudentId())
                .admissionYear(user.getAdmissionYear())
                .profileImage(user.getProfileImage())
                .posts(post)
                .build();
    }
}