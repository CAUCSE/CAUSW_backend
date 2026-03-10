package net.causw.app.main.domain.community.ceremony.util;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.ceremony.api.v2.dto.request.CreateCeremonyRequest;
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

	public void validateForCreate(CreateCeremonyRequest dto) {
		ceremonyCategoryValidator.validateCustomCategory(dto.ceremonyCategory(), dto.ceremonyCustomCategory());

		ceremonyRelationValidator.validateRelation(dto.relationType(), dto.familyRelation(),
			dto.alumniRelation(), dto.alumniName(), dto.alumniAdmissionYear());

		ceremonyDateTimeValidator.validateDateTime(dto.startDate(), dto.endDate(), dto.startTime(),
			dto.endTime());
		ceremonyDateTimeValidator.validateDateTimeRange(dto.startDate(), dto.endDate(), dto.startTime(),
			dto.endTime());

		ceremonyNotificationValidator.validateNotificationTarget(dto.isSetAll(), dto.targetAdmissionYears());
	}

	public void validateAwaiting(Ceremony ceremony) {
		if (ceremony.getCeremonyState() != CeremonyState.AWAIT) {
			throw CeremonyErrorCode.CEREMONY_ALREADY_PROCESSED.toBaseException();
		}
	}
}
