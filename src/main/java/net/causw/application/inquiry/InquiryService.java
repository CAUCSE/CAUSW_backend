package net.causw.application.inquiry;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.inquiry.Inquiry;
import net.causw.adapter.persistence.port.inquiry.InquiryRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.inquiry.InquiryCreateRequestDto;
import net.causw.application.dto.inquiry.InquiryResponseDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.valid.UserValid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;

@Service
@RequiredArgsConstructor
public class InquiryService {
    private final InquiryRepository inquiryRepository;
    private final Validator validator;

    @Transactional(readOnly = true)
    public InquiryResponseDto findById(
            @UserValid User user,
            String inquiryId
    ) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.INQUIRY_NOT_FOUND
                )
        );
        new TargetIsDeletedValidator().validate(inquiry.getIsDeleted(), StaticValue.DOMAIN_INQUIRY);

        return InquiryResponseDto.of(
                inquiry,
                user
        );
    }

    @Transactional
    public InquiryResponseDto create(
            @UserValid User user,
            InquiryCreateRequestDto inquiryCreateRequestDto
    ) {
        Inquiry inquiry = Inquiry.of(
                inquiryCreateRequestDto.getTitle(),
                inquiryCreateRequestDto.getContent(),
                user
        );
        new ConstraintValidator<Inquiry>().validate(inquiry, validator);

        return InquiryResponseDto.of(
                inquiryRepository.save(inquiry),
                user
        );
    }
}
