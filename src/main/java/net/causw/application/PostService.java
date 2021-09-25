package net.causw.application;

import net.causw.application.dto.PostCreateRequestDto;
import net.causw.application.dto.PostResponseDto;
import net.causw.application.spi.BoardPort;
import net.causw.application.spi.PostPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.ArrayList;

@Service
public class PostService {
    private final PostPort postPort;
    private final UserPort userPort;
    private final BoardPort boardPort;
    private final Validator validator;

    public PostService(
            PostPort postPort,
            UserPort userPort,
            BoardPort boardPort,
            Validator validator
    ) {
        this.postPort = postPort;
        this.userPort = userPort;
        this.boardPort = boardPort;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public PostResponseDto findById(String id) {
        // TODO : GHJANG : Need implementation - put comment (with pagenation)
        return PostResponseDto.from(this.postPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid post id"
                )
        ), new ArrayList<>());
    }

    @Transactional
    public PostResponseDto create(String creatorId, PostCreateRequestDto postCreateRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel creatorDomainModel = this.userPort.findById(creatorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid request user id"
                )
        );

        BoardDomainModel boardDomainModel = this.boardPort.findById(postCreateRequestDto.getBoardId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid board id"
                )
        );

        // TODO : GHJANG : validate create role of board for creator user
        // TODO : GHJANG : If the board has Circle, member check is also needed

        PostDomainModel postDomainModel = PostDomainModel.of(
                postCreateRequestDto.getTitle(),
                postCreateRequestDto.getContent(),
                creatorDomainModel,
                boardDomainModel
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(postDomainModel, this.validator))
                .validate();

        return PostResponseDto.from(this.postPort.create(postDomainModel));
    }
}
