package net.causw.domain.model.inquiry;

import lombok.Builder;
import lombok.Getter;
import net.causw.domain.model.user.UserDomainModel;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Builder
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

    public static InquiryDomainModel of(
            String id,
            String title,
            String content,
            UserDomainModel writer,
            Boolean isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return InquiryDomainModel.builder()
                .id(id)
                .title(title)
                .content(content)
                .writer(writer)
                .isDeleted(isDeleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static InquiryDomainModel of(
            String title,
            String content,
            UserDomainModel writer
    ) {
        return InquiryDomainModel.builder()
                .title(title)
                .content(content)
                .writer(writer)
                .build();
    }
}
