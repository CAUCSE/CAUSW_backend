package net.causw.application;

import net.causw.application.dto.HomePageResponseDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.post.PostsResponseDto;
import net.causw.application.spi.BoardPort;
import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CommentPort;
import net.causw.application.spi.FavoriteBoardPort;
import net.causw.application.spi.PostPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.CircleMemberDomainModel;
import net.causw.domain.model.CircleMemberStatus;
import net.causw.domain.model.FavoriteBoardDomainModel;
import net.causw.domain.model.StaticValue;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class HomePageService {
    private final FavoriteBoardPort favoriteBoardPort;
    private final UserPort userPort;
    private final BoardPort boardPort;
    private final PostPort postPort;
    private final CommentPort commentPort;
    private final CircleMemberPort circleMemberPort;

    public HomePageService(
            FavoriteBoardPort favoriteBoardPort,
            UserPort userPort,
            BoardPort boardPort,
            PostPort postPort,
            CommentPort commentPort,
            CircleMemberPort circleMemberPort
    ) {
        this.favoriteBoardPort = favoriteBoardPort;
        this.userPort = userPort;
        this.boardPort = boardPort;
        this.postPort = postPort;
        this.commentPort = commentPort;
        this.circleMemberPort = circleMemberPort;
    }

    @Transactional
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

        List<FavoriteBoardDomainModel> favoriteBoardDomainModelList = this.favoriteBoardPort.findByUserId(userId);
        AtomicBoolean requireReload = new AtomicBoolean(false);

        // Delete favorite board if
        // 1) The board is deleted
        // 2) The circle is deleted if the board is circle's one
        // 3) The user is not a member of circle if the board is circle's one
        favoriteBoardDomainModelList.forEach(
                favoriteBoardDomainModel -> {
                    if (favoriteBoardDomainModel.getBoardDomainModel().getIsDeleted()) {
                        this.favoriteBoardPort.delete(favoriteBoardDomainModel);
                        requireReload.set(true);
                        return;
                    }

                    favoriteBoardDomainModel.getBoardDomainModel().getCircle().ifPresent(
                            circleDomainModel -> {
                                if (circleDomainModel.getIsDeleted()) {
                                    this.favoriteBoardPort.delete(favoriteBoardDomainModel);
                                    requireReload.set(true);
                                    return;
                                }

                                CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(userId, circleDomainModel.getId()).orElse(null);
                                if (circleMemberDomainModel == null ||
                                        circleMemberDomainModel.getStatus() != CircleMemberStatus.MEMBER
                                ) {
                                    this.favoriteBoardPort.delete(favoriteBoardDomainModel);
                                    requireReload.set(true);
                                }
                            }
                    );
                }
        );

        if (requireReload.get()) {
            favoriteBoardDomainModelList = this.favoriteBoardPort.findByUserId(userId);
        }

        // Create default favorite board if not exist
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
