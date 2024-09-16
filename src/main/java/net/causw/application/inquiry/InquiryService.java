package net.causw.application.inquiry;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.inquiry.Inquiry;
import net.causw.adapter.persistence.repository.inquiry.InquiryRepository;
import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.inquiry.InquiryCreateRequestDto;
import net.causw.application.dto.inquiry.InquiryResponseDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.ValidatorBucket;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.ConstraintValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final Validator validator;
    private final UserRepository userRepository;
    private final InquiryRepository inquiryRepository;

    @Transactional(readOnly = true)
    public InquiryResponseDto findById(
            String requestUserId,
            String inquiryId
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        User user = userRepository.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.INQUIRY_NOT_FOUND
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRoles()))
                .consistOf(TargetIsDeletedValidator.of(inquiry.getIsDeleted(), StaticValue.DOMAIN_INQUIRY));

        validatorBucket
                .validate();

        return InquiryResponseDto.of(
                inquiry,
                user
        );
    }

    @Transactional
    public InquiryResponseDto create(String requestUserId, InquiryCreateRequestDto inquiryCreateRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        User writer = userRepository.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(writer.getState()))
                .consistOf(UserRoleIsNoneValidator.of(writer.getRoles()));

        Inquiry inquiry = Inquiry.of(
                inquiryCreateRequestDto.getTitle(),
                inquiryCreateRequestDto.getContent(),
                writer
        );

        inquiryRepository.save(inquiry);

        validatorBucket
                .consistOf(ConstraintValidator.of(inquiry, this.validator))
                .validate();

        return InquiryResponseDto.of(
                inquiryRepository.save(inquiry),
                writer
        );
    }

}
