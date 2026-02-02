package net.causw.app.main.domain.community.board.api.v2.dto.response;

import java.util.List;

import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;

import io.swagger.v3.oas.annotations.media.Schema;

public record BoardConfigEditResponse(
	@Schema(description = "게시판 고유 아이디", example = "b1a2b3c4-d5e6-7f89-0a1b-2c3d4e5f6a7b") String boardId,
	@Schema(description = "게시판 이름", example = "자유 게시판") String name,
	@Schema(description = "게시판 설명", example = "학교 생활과 관련된 자유로운 이야기를 나누는 공간입니다.") String description,
	@Schema(description = "익명 게시판 여부", example = "true") Boolean isAnonymous,
	@Schema(description = "읽기 권한 범위", example = "ENROLLED") BoardReadScope readScope,
	@Schema(description = "쓰기 권한 범위", example = "ONLY_ADMIN") BoardWriteScope writeScope,
	@Schema(description = "공지 게시판 여부", example = "false") Boolean isNotice,
	@Schema(description = "게시판 노출 여부", example = "VISIBLE") BoardVisibility visibility,
	@Schema(description = "게시판 표시 순서", example = "1") Integer displayOrder,
	@Schema(description = "게시판 관리자 목록") List<AdminResponse> admins) {
	public record AdminResponse(
		@Schema(description = "관리자 고유 아이디", example = "uuid-1234-5678-9012-abcdefg") String id,
		@Schema(description = "관리자 이메일", example = "test@test.com") String adminEmail,
		@Schema(description = "관리자 이름", example = "홍길동") String adminName) {
	}
}
