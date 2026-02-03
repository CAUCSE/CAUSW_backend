package net.causw.app.main.domain.community.board.api.v2.dto.request;

import java.util.List;

import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BoardCreateRequest(
	@Schema(description = "게시판 이름", example = "자유 게시판1") @NotBlank String name,
	@Schema(description = "게시판 설명", example = "자유 게시판입니다.") String description,
	@Schema(description = "게시판 관리자 사용자 아이디 목록") @NotNull List<String> adminUserIds,
	@Schema(description = "익명 게시판 여부", example = "true") @NotNull Boolean isAnonymous,
	@Schema(description = "읽기 권한 범위", example = "ENROLLED") @NotNull BoardReadScope readScope,
	@Schema(description = "쓰기 권한 범위", example = "ALL_USER") @NotNull BoardWriteScope writeScope,
	@Schema(description = "공지 게시판 여부", example = "false") @NotNull Boolean isNotice,
	@Schema(description = "게시판 노출 여부", example = "VISIBLE") @NotNull BoardVisibility visibility) {
}
