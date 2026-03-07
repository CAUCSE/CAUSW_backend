package net.causw.app.main.domain.community.ceremony.util;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.ceremony.api.v2.dto.request.CreateCeremonyRequestDto;
import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;
import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CeremonyValidator {

	private final CeremonyRelationValidator ceremonyRelationValidator;
	private final CeremonyDateTimeValidator ceremonyDateTimeValidator;
	private final CeremonyNotificationValidator ceremonyNotificationValidator;
	private final CeremonyCategoryValidator ceremonyCategoryValidator;

	public void validateForCreate(CreateCeremonyRequestDto dto) {
		ceremonyCategoryValidator.validateCustomCategory(dto.getCeremonyCategory(), dto.getCeremonyCustomCategory());

		ceremonyRelationValidator.validateRelation(dto.getRelationType(), dto.getFamilyRelation(),
			dto.getAlumniRelation(), dto.getAlumniName(), dto.getAlumniAdmissionYear());

		ceremonyDateTimeValidator.validateDateTime(dto.getStartDate(), dto.getEndDate(), dto.getStartTime(),
			dto.getEndTime());
		ceremonyDateTimeValidator.validateDateTimeRange(dto.getStartDate(), dto.getEndDate(), dto.getStartTime(),
			dto.getEndTime());

		ceremonyNotificationValidator.validateNotificationTarget(dto.getIsSetAll(), dto.getTargetAdmissionYears());
	}

	public void validateAwaiting(Ceremony ceremony) {
		if (ceremony.getCeremonyState() != CeremonyState.AWAIT) {
			throw CeremonyErrorCode.CEREMONY_ALREADY_PROCESSED.toBaseException();
		}
	}
}
