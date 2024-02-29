package net.causw.application.homepage;

import net.causw.application.dto.homepage.HomePageResponseDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.post.PostsResponseDto;
import net.causw.application.spi.BoardPort;
import net.causw.application.spi.CommentPort;
import net.causw.application.spi.FavoriteBoardPort;
import net.causw.application.spi.PostPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.board.BoardDomainModel;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.model.user.UserDomainModel;
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


        List<BoardDomainModel> boardDomainModelList = this.boardPort.findAllBoard(false);
        if(boardDomainModelList.isEmpty()){
            throw new BadRequestException(
                    ErrorCode.ROW_DOES_NOT_EXIST,
                    "게시판을 찾을 수 없습니다."
            );
        }
        return boardDomainModelList
                .stream()
                .filter(board -> board.getCircle().isEmpty())
                .map(boardDomainModel -> HomePageResponseDto.from(
                        BoardResponseDto.from(boardDomainModel, userDomainModel.getRole()),
                        this.postPort.findAllPost(
                                boardDomainModel.getId(),
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
