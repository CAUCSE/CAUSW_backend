package net.causw.application.dto.inquiry;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.InquiryDomainModel;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;

import java.time.LocalDateTime;

@Getter
@Setter
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

    public InquiryResponseDto(
            String id,
            String title,
            String content,
            Boolean isDeleted,
            String writerName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
        this.writerName = writerName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static InquiryResponseDto from(
            InquiryDomainModel inquiry,
            UserDomainModel user
    ){
        boolean updatable = false;
        boolean deletable = false;

        if (user.getRole() == Role.ADMIN) {
            updatable = true;
            deletable = true;
        } else if (inquiry.getWriter().getId().equals(user.getId())) {
            updatable = true;
            deletable = true;
        } else {

        }

        return new InquiryResponseDto(
                inquiry.getId(),
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.getIsDeleted(),
                inquiry.getWriter().getName(),
                inquiry.getCreatedAt(),
                inquiry.getUpdatedAt()
        );
    }
}
