package net.causw.application.dto.post;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.post.PostDomainModel;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostsResponseDto {
    @ApiModelProperty(value = "게시글 id", example = "uuid 형식의 String 값입니다.")
    private String id;

    @ApiModelProperty(value = "게시글 제목", example = "게시글의 제목입니다.")
    private String title;

    @ApiModelProperty(value = "게시글 작성자 이름", example = "관리자")
    private String writerName;

    @ApiModelProperty(value = "게시글 작성자의 승인년도", example = "2020")
    private Integer writerAdmissionYear;

    @ApiModelProperty(value = "답글 개수", example = "13")
    private Long numComment;

    @ApiModelProperty(value = "게시글 생성 시간", example =  "2024-01-26T18:40:40.643Z")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "게시글 업데이트 시간", example =  "2024-01-26T18:40:40.643Z")
    private LocalDateTime updatedAt;

    @ApiModelProperty(value = "게시글 삭제여부", example = "false")
    private Boolean isDeleted;

    private PostsResponseDto(
            String id,
            String title,
            String writerName,
            Integer writerAdmissionYear,
            Long numComment,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted
    ) {
        this.id = id;
        this.title = title;
        this.writerName = writerName;
        this.writerAdmissionYear = writerAdmissionYear;
        this.numComment = numComment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    public static PostsResponseDto from(
            PostDomainModel post,
            Long numComment
    ) {
        return new PostsResponseDto(
                post.getId(),
                post.getTitle(),
                post.getWriter().getName(),
                post.getWriter().getAdmissionYear(),
                numComment,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getIsDeleted()
        );
    }
}