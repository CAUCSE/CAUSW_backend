package net.causw.application.dto.vote;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class CastVoteRequestDto {

    @NotEmpty(message = "투표 옵션 ID 리스트는 비어 있을 수 없습니다.")
    private List<String> voteOptionIdList;
}
