package net.causw.app.main.domain.community.board.api.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardNameCheckResponseDto {
	Boolean isPresent;
}
