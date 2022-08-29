package net.causw.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class InquiryDomainModel {

    private String id;

    @NotBlank(message = "문의글 제목이 입력되지 않았습니다.")
    private String title;
    private String content;

    @NotNull(message = "작성자가 입력되지 않았습니다.")
    private UserDomainModel writer;

    @NotNull(message = "문의글 상태가 입력되지 않았습니다.")
    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private InquiryDomainModel(
            String id,
            String title,
            String content,
            UserDomainModel writer,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static InquiryDomainModel of(
            String id,
            String title,
            String content,
            UserDomainModel writer,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new InquiryDomainModel(
                id,
                title,
                content,
                writer,
                isDeleted,
                createdAt,
                updatedAt
        );
    }
    public static InquiryDomainModel of(
            String title,
            String content,
            UserDomainModel writer
    ) {
        return new InquiryDomainModel(
                null,
                title,
                content,
                writer,
                false,
                null,
                null
        );
    }

}
