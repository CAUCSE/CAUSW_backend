package net.causw.app.main.dto.inquiry;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.app.main.domain.model.entity.inquiry.Inquiry;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.user.Role;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class InquiryResponseDto {
    private String id;
    private String title;
    private String content;
    private Boolean isDeleted;
    private String writerName;
    private Boolean updatable;
    private Boolean deletable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InquiryResponseDto of(
            Inquiry inquiry,
            User user
    ){
        boolean updatable = false;
        boolean deletable = false;

        if (inquiry.getWriter().getRoles().contains(Role.ADMIN) || user.getId().equals(inquiry.getWriter().getId())) {
            updatable = true;
            deletable = true;
        }

        return InquiryResponseDto.builder()
                .id(inquiry.getId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .isDeleted(inquiry.getIsDeleted())
                .writerName(inquiry.getWriter().getName())
                .updatable(updatable)
                .deletable(deletable)
                .createdAt(inquiry.getCreatedAt())
                .updatedAt(inquiry.getUpdatedAt())
                .build();
    }
}
