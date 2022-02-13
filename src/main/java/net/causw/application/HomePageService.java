package net.causw.application;

import net.causw.application.dto.BoardResponseDto;
import net.causw.application.dto.HomePageResponseDto;
import net.causw.application.dto.PostAllResponseDto;
import net.causw.application.spi.CommentPort;
import net.causw.application.spi.FavoriteBoardPort;
import net.causw.application.spi.PostPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
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
    private final PostPort postPort;
    private final CommentPort commentPort;

    public HomePageService(
            FavoriteBoardPort favoriteBoardPort,
            UserPort userPort,
            PostPort postPort,
            CommentPort commentPort
    ) {
        this.favoriteBoardPort = favoriteBoardPort;
        this.userPort = userPort;
        this.postPort = postPort;
        this.commentPort = commentPort;
    }

    public List<HomePageResponseDto> getHomePage(String userId) {
        UserDomainModel user = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .validate();

        return this.favoriteBoardPort.findByUserId(userId)
                .stream()
                .map(favoriteBoardDomainModel -> HomePageResponseDto.from(
                        BoardResponseDto.from(favoriteBoardDomainModel.getBoardDomainModel(), user.getRole()),
                        this.postPort.findAll(
                                favoriteBoardDomainModel.getBoardDomainModel().getId(),
                                0,
                                StaticValue.HOME_POST_PAGE_SIZE
                        )
                                .map(postDomainModel -> PostAllResponseDto.from(
                                        postDomainModel,
                                        this.commentPort.countByPostId(postDomainModel.getId())
                                ))
                        )
                )
                .collect(Collectors.toList());
    }
}
