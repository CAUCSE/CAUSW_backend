package net.causw.app.main.domain.community.ceremony.util;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.ceremony.api.v2.dto.request.CreateCeremonyRequestDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CeremonyValidator {

	private final CeremonyRelationValidator ceremonyRelationValidator;
	private final CeremonyDateTimeValidator ceremonyDateTimeValidator;
	private final CeremonyNotificationValidator ceremonyNotificationValidator;

	public void validateForCreate(CreateCeremonyRequestDto dto) {

		ceremonyRelationValidator.validateRelation(dto.getRelationType(), dto.getFamilyRelation(),
			dto.getAlumniRelation(), dto.getAlumniName(), dto.getAlumniAdmissionYear());

		ceremonyDateTimeValidator.validateDateTime(dto.getEndDate(), dto.getStartTime(), dto.getEndTime());
		ceremonyDateTimeValidator.validateDateTimeRange(dto.getStartDate(), dto.getEndDate(), dto.getStartTime(),
			dto.getEndTime());

		ceremonyNotificationValidator.validateNotificationTarget(dto.getIsSetAll(), dto.getTargetAdmissionYears());
	}
}
