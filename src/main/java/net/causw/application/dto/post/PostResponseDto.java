package net.causw.application.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.post.Post;
import net.causw.application.dto.file.FileResponseDto;
import net.causw.application.dto.comment.CommentResponseDto;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PostResponseDto {
    @Schema(description = "게시글 id", example = "uuid 형식의 String 값입니다.")
    private String id;

    @Schema(description = "게시글 제목", example = "게시글의 제목입니다.")
    private String title;

    @Schema(description = "게시글 내용", example = "안녕하세요. 학생회입니다. 공지사항입니다.")
    private String content;

    @Schema(description = "게시글 삭제여부", example = "false")
    private Boolean isDeleted;

    @Schema(description = "게시글 작성자 이름", example = "관리자")
    private String writerName;

    @Schema(description = "게시글 작성자의 승인년도", example = "2020")
    private Integer writerAdmissionYear;

    @Schema(description = "게시글 작성자의 프로필 이미지", example = "프로필 이미지 url 작성")
    private String writerProfileImage;

    @Schema(description = "첨부파일", example = "첨부파일 url 작성")
    private List<FileResponseDto> attachmentList;

    @Schema(description = "답글 개수", example = "13")
    private Long numComment;

    @Schema(description = "익명글 여부", example = "False")
    private Boolean isAnonymous;

    @Schema(description = "질문글 여부", example = "False")
    private Boolean isQuestion;

    @Schema(description = "게시글 업데이트 가능여부", example = "true")
    private Boolean updatable;

    @Schema(description = "게시글 삭제 가능여부", example = "true")
    private Boolean deletable;

    @Schema(description = "게시글 생성 시간", example = "2024-01-26T18:40:40.643Z")
    private LocalDateTime createdAt;

    @Schema(description = "게시글 업데이트 시간", example = "2024-01-26T18:40:40.643Z")
    private LocalDateTime updatedAt;

    @Schema(description = "게시글의 답글 정보", example = "답글에 대한 정보 조회")
    private Page<CommentResponseDto> commentList;

    @Schema(description = "게시판 이름", example = "게시판 이름입니다.")
    private String boardName;

    // FIXME: 리팩토링 후 삭제예정
    // 생성, 삭제
    public static PostResponseDto of(
            Post post,
            boolean updatable,
            boolean deletable
    ) {
        //List<String> attachmentList = post.getAttachments().map(attachments -> Arrays.asList(attachments.split(":::"))).orElse(List.of());
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .isDeleted(post.getIsDeleted())
                .writerName(post.getWriter().getName())
                .writerAdmissionYear(post.getWriter().getAdmissionYear())
                .writerProfileImage(post.getWriter().getProfileImage())
                //.attachmentList(attachmentList.stream().map(FileResponseDto::from).collect(Collectors.toList()))
                .numComment(0L)
                .updatable(updatable)
                .deletable(deletable)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    // 조회, 수정, 복원
    public static PostResponseDto of(
            Post post,
            Page<CommentResponseDto> commentList,
            Long numComment,
            boolean updatable,
            boolean deletable
    ) {
        //List<String> attachmentList = post.getAttachments().map(attachments -> Arrays.asList(attachments.split(":::"))).orElse(List.of());
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .isDeleted(post.getIsDeleted())
                .writerName(post.getWriter().getName())
                .writerAdmissionYear(post.getWriter().getAdmissionYear())
                .writerProfileImage(post.getWriter().getProfileImage())
                //.attachmentList(attachmentList.stream().map(FileResponseDto::from).collect(Collectors.toList()))
                .numComment(numComment)
                .updatable(updatable)
                .deletable(deletable)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .commentList(commentList)
                .boardName(post.getBoard().getName())
                .build();
    }
}
