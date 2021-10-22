package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.Role;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BoardOfCircleResponseDto {
    private String id;
    private String name;
    private List<String> createRoleList;
    private String category;
    private Boolean writable;
    private Boolean isDeleted;

    private String circleId;
    private String circleName;

    private String postId;
    private String postWriterName;
    private LocalDateTime postCreatedAt;
    private Long postNumComment;

    private BoardOfCircleResponseDto(
            String id,
            String name,
            List<String> createRoleList,
            String category,
            Boolean writable,
            Boolean isDeleted,
            String circleId,
            String circleName,
            String postId,
            String postWriterName,
            LocalDateTime postCreatedAt,
            Long postNumComment
    ) {
        this.id = id;
        this.name = name;
        this.createRoleList = createRoleList;
        this.category = category;
        this.writable = writable;
        this.isDeleted = isDeleted;
        this.circleId = circleId;
        this.circleName = circleName;
        this.postId = postId;
        this.postWriterName = postWriterName;
        this.postCreatedAt = postCreatedAt;
        this.postNumComment = postNumComment;
    }

    public static BoardOfCircleResponseDto from(
            BoardDomainModel boardDomainModel,
            Role userRole,
            PostDomainModel postDomainModel,
            Long numComment
    ) {
        String circleId = boardDomainModel.getCircle().map(CircleDomainModel::getId).orElse(null);
        String circleName = boardDomainModel.getCircle().map(CircleDomainModel::getName).orElse(null);

        return new BoardOfCircleResponseDto(
                boardDomainModel.getId(),
                boardDomainModel.getName(),
                boardDomainModel.getCreateRoleList(),
                boardDomainModel.getCategory(),
                boardDomainModel.getCreateRoleList().contains(userRole.getValue()),
                boardDomainModel.getIsDeleted(),
                circleId,
                circleName,
                postDomainModel.getId(),
                postDomainModel.getWriter().getName(),
                postDomainModel.getCreatedAt(),
                numComment
        );
    }

    public static BoardOfCircleResponseDto from(
            BoardDomainModel boardDomainModel,
            Role userRole
    ) {
        String circleId = boardDomainModel.getCircle().map(CircleDomainModel::getId).orElse(null);
        String circleName = boardDomainModel.getCircle().map(CircleDomainModel::getName).orElse(null);

        return new BoardOfCircleResponseDto(
                boardDomainModel.getId(),
                boardDomainModel.getName(),
                boardDomainModel.getCreateRoleList(),
                boardDomainModel.getCategory(),
                boardDomainModel.getCreateRoleList().contains(userRole.getValue()),
                boardDomainModel.getIsDeleted(),
                circleId,
                circleName,
                null,
                null,
                null,
                0L
        );
    }
}
