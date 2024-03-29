package net.causw.application.inquiry;

import lombok.RequiredArgsConstructor;
import net.causw.application.dto.inquiry.InquiryCreateRequestDto;
import net.causw.application.dto.inquiry.InquiryResponseDto;
import net.causw.application.spi.InquiryPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.inquiry.InquiryDomainModel;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.validation.ValidatorBucket;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.ConstraintValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final UserPort userPort;
    private final Validator validator;
    private final InquiryPort inquiryPort;

    @Transactional(readOnly = true)
    public InquiryResponseDto findById(
            String requestUserId,
            String inquiryId
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel userDomainModel = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        InquiryDomainModel inquiryDomainModel = this.inquiryPort.findById(inquiryId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "문의글을 찾을 수 없습니다."
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(userDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(userDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(inquiryDomainModel.getIsDeleted(), StaticValue.DOMAIN_INQUIRY));

        validatorBucket
                .validate();

        return InquiryResponseDto.from(
                inquiryDomainModel,
                userDomainModel
        );
    }

    @Transactional
    public InquiryResponseDto create(String requestUserId, InquiryCreateRequestDto inquiryCreateRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel creatorDomainModel = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(creatorDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(creatorDomainModel.getRole()));

        InquiryDomainModel inquiryDomainModel = InquiryDomainModel.of(
                inquiryCreateRequestDto.getTitle(),
                inquiryCreateRequestDto.getContent(),
                creatorDomainModel
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(inquiryDomainModel, this.validator))
                .validate();

        return InquiryResponseDto.from(
                this.inquiryPort.create(inquiryDomainModel),
                creatorDomainModel
        );
    }

}
