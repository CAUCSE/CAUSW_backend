package net.causw.application;

import net.causw.application.dto.HomePageResponseDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.post.PostsResponseDto;
import net.causw.application.spi.BoardPort;
import net.causw.application.spi.CommentPort;
import net.causw.application.spi.FavoriteBoardPort;
import net.causw.application.spi.PostPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.FavoriteBoardDomainModel;
import net.causw.domain.model.StaticValue;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HomePageService {
    private final FavoriteBoardPort favoriteBoardPort;
    private final UserPort userPort;
    private final BoardPort boardPort;
    private final PostPort postPort;
    private final CommentPort commentPort;

    public HomePageService(
            FavoriteBoardPort favoriteBoardPort,
            UserPort userPort,
            BoardPort boardPort,
            PostPort postPort,
            CommentPort commentPort
    ) {
        this.favoriteBoardPort = favoriteBoardPort;
        this.userPort = userPort;
        this.boardPort = boardPort;
        this.postPort = postPort;
        this.commentPort = commentPort;
    }

    public List<HomePageResponseDto> getHomePage(String userId) {
        UserDomainModel userDomainModel = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(userDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(userDomainModel.getRole()))
                .validate();

        // Create default favorite board if not exist
        List<FavoriteBoardDomainModel> favoriteBoardDomainModelList = this.favoriteBoardPort.findByUserId(userId);
        if (favoriteBoardDomainModelList.isEmpty()) {
            favoriteBoardDomainModelList = this.boardPort.findBasicBoards()
                    .stream()
                    .map(boardDomainModel ->
                            this.favoriteBoardPort.create(FavoriteBoardDomainModel.of(
                                    userDomainModel,
                                    boardDomainModel
                            ))
                    )
                    .collect(Collectors.toList());
        }

        return favoriteBoardDomainModelList
                .stream()
                .map(favoriteBoardDomainModel -> HomePageResponseDto.from(
                        BoardResponseDto.from(favoriteBoardDomainModel.getBoardDomainModel(), userDomainModel.getRole()),
                        this.postPort.findAll(
                                favoriteBoardDomainModel.getBoardDomainModel().getId(),
                                0,
                                StaticValue.HOME_POST_PAGE_SIZE
                        ).map(postDomainModel -> PostsResponseDto.from(
                                postDomainModel,
                                this.commentPort.countByPostId(postDomainModel.getId())
                        )))
                )
                .collect(Collectors.toList());
    }
}
