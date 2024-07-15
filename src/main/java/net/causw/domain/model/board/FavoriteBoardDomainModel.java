package net.causw.domain.model.board;

import lombok.Builder;
import lombok.Getter;
import net.causw.domain.model.user.UserDomainModel;

import jakarta.validation.constraints.NotNull;

@Getter
@Builder
public class FavoriteBoardDomainModel {
    private String id;

    @NotNull(message = "사용자가 입력되지 않았습니다.")
    private UserDomainModel userDomainModel;

    private BoardDomainModel boardDomainModel;

    public static FavoriteBoardDomainModel of(
            UserDomainModel userDomainModel,
            BoardDomainModel boardDomainModel
    ) {
        return FavoriteBoardDomainModel.builder()
                .userDomainModel(userDomainModel)
                .boardDomainModel(boardDomainModel)
                .build();
    }

    public static FavoriteBoardDomainModel of(
            String id,
            UserDomainModel userDomainModel,
            BoardDomainModel boardDomainModel
    ) {
        return FavoriteBoardDomainModel.builder()
                .id(id)
                .userDomainModel(userDomainModel)
                .boardDomainModel(boardDomainModel)
                .build();
    }
}
