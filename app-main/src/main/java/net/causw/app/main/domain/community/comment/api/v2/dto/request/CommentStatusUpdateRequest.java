package net.causw.app.main.domain.community.comment.api.v2.dto.request;

import net.causw.app.main.domain.community.comment.enums.CommentAdminStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CommentStatusUpdateRequest(
	@NotNull @Schema(description = "변경할 댓글 상태", example = "DELETED") CommentAdminStatus status) {
}
