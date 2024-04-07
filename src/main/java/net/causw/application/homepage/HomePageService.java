package net.causw.application.homepage;

import lombok.RequiredArgsConstructor;
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
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomePageService {
    private final FavoriteBoardPort favoriteBoardPort;
    private final UserPort userPort;
    private final BoardPort boardPort;
    private final PostPort postPort;
    private final CommentPort commentPort;

    public List<HomePageResponseDto> getHomePage(String userId) {
        UserDomainModel userDomainModel = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
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
                    MessageUtil.BOARD_NOT_FOUND
            );
        }
        return boardDomainModelList
                .stream()
                .map(boardDomainModel -> HomePageResponseDto.from(
                        BoardResponseDto.from(boardDomainModel, userDomainModel.getRole()),
                        this.postPort.findAllPost(
                                boardDomainModel.getId(),
                                0,
                                StaticValue.HOME_POST_PAGE_SIZE
                        ).map(postDomainModel -> PostsResponseDto.from(
                                postDomainModel,
                                this.postPort.countAllComment(postDomainModel.getId())
                        )))
                )
                .collect(Collectors.toList());
    }
}
