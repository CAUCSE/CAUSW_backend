package net.causw.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class FavoriteBoardDomainModel {
    private String id;

    @NotNull(message = "사용자가 입력되지 않았습니다.")
    private UserDomainModel userDomainModel;
    private BoardDomainModel boardDomainModel;

    private FavoriteBoardDomainModel(
            String id,
            UserDomainModel userDomainModel,
            BoardDomainModel boardDomainModel
    ) {
        this.id = id;
        this.userDomainModel = userDomainModel;
        this.boardDomainModel = boardDomainModel;
    }

    public static FavoriteBoardDomainModel of(
            UserDomainModel userDomainModel,
            BoardDomainModel boardDomainModel
    ) {
        return new FavoriteBoardDomainModel(
                null,
                userDomainModel,
                boardDomainModel
        );
    }

    public static FavoriteBoardDomainModel of(
            String id,
            UserDomainModel userDomainModel,
            BoardDomainModel boardDomainModel
    ) {
        return new FavoriteBoardDomainModel(
                id,
                userDomainModel,
                boardDomainModel
        );
    }
}
