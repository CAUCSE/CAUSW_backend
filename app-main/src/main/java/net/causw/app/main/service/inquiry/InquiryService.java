package net.causw.app.main.service.inquiry;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.model.entity.inquiry.Inquiry;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.validation.ConstraintValidator;
import net.causw.app.main.domain.validation.TargetIsDeletedValidator;
import net.causw.app.main.domain.validation.UserRoleIsNoneValidator;
import net.causw.app.main.domain.validation.UserStateValidator;
import net.causw.app.main.domain.validation.ValidatorBucket;
import net.causw.app.main.dto.inquiry.InquiryCreateRequestDto;
import net.causw.app.main.dto.inquiry.InquiryResponseDto;
import net.causw.app.main.infrastructure.aop.annotation.MeasureTime;
import net.causw.app.main.repository.inquiry.InquiryRepository;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@MeasureTime
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
