package net.causw.app.main.domain.community.board.service.dto.result;

import java.util.List;

import lombok.Builder;

/**
 * 게시판 설정 목록 조회 결과를 나타내는 application DTO.
 * 목록 한 항목은 {@link BoardConfigSummary}로 재사용한다.
 * Entity → DTO 변환은 {@link net.causw.app.main.domain.community.board.service.dto.mapper.BoardConfigSummaryMapper}를 사용해 Service에서 조합한다.
 */
@Builder
public record BoardConfigListResult(List<BoardConfigSummary> boards) {
}
