package net.causw.app.main.domain.community.ceremony.validation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.ceremony.api.v2.dto.request.CreateCeremonyRequestDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CeremonyValidator {

	private final CeremonyCategoryValidator ceremonyCategoryValidator;
	private final CeremonyRelationValidator ceremonyRelationValidator;
	private final CeremonyDateTimeValidator ceremonyDateTimeValidator;
	private final CeremonyNotificationValidator ceremonyNotificationValidator;

	public void validateForCreate(CreateCeremonyRequestDto dto) {

		ceremonyCategoryValidator.validateCategory(dto.getCeremonyCategory(), dto.getCeremonyCustomCategory());

		ceremonyRelationValidator.validateRelation(dto.getRelationType(), dto.getFamilyRelation(),
			dto.getAlumniRelation(), dto.getAlumniName(), dto.getAlumniAdmissionYear());

		ceremonyDateTimeValidator.validateEndTime(dto.getEndDate(), dto.getEndTime(), dto.getStartTime());

		ceremonyNotificationValidator.validateNotificationTarget(dto.getIsSetAll(), dto.getTargetAdmissionYears());
	}
}
