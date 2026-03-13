package net.causw.app.main.domain.community.ceremony.util;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;
import net.causw.app.main.domain.community.ceremony.service.dto.request.CeremonyCreateCommand;
import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CeremonyValidator {

	private final CeremonyRelationValidator ceremonyRelationValidator;
	private final CeremonyDateTimeValidator ceremonyDateTimeValidator;
	private final CeremonyNotificationValidator ceremonyNotificationValidator;
	private final CeremonyCategoryValidator ceremonyCategoryValidator;

	public void validateForCreate(CeremonyCreateCommand command) {
		ceremonyCategoryValidator.validateCustomCategory(command.ceremonyCategory(), command.ceremonyCustomCategory());

		ceremonyRelationValidator.validateRelation(command.relationType(), command.familyRelation(),
			command.alumniRelation(), command.alumniName(), command.alumniAdmissionYear());

		ceremonyDateTimeValidator.validateDateTime(command.startDate(), command.endDate(), command.startTime(),
			command.endTime());
		ceremonyDateTimeValidator.validateDateTimeRange(command.startDate(), command.endDate(), command.startTime(),
			command.endTime());

		ceremonyNotificationValidator.validateNotificationTarget(command.isSetAll(), command.targetAdmissionYears());
	}

	public void validateAwaiting(Ceremony ceremony) {
		if (ceremony.getCeremonyState() != CeremonyState.AWAIT) {
			throw CeremonyErrorCode.CEREMONY_ALREADY_PROCESSED.toBaseException();
		}
	}
}
