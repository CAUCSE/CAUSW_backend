package net.causw.app.main.dto.vote;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CastVoteRequestDto {

	@NotEmpty(message = "투표 옵션 ID 리스트는 비어 있을 수 없습니다.")
	private List<String> voteOptionIdList;
}
