package net.causw.app.main.domain.community.ceremony.validation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.ceremony.enums.AlumniRelation;
import net.causw.app.main.domain.community.ceremony.enums.FamilyRelation;
import net.causw.app.main.domain.community.ceremony.enums.RelationType;
import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;

@Component
public class CeremonyRelationValidator {

	public void validateRelation(RelationType relationType, FamilyRelation familyRelation,
		AlumniRelation alumniRelation, String alumniName, String alumniAdmissionYear) {
		switch (relationType) {
			case FAMILY -> {
				if (familyRelation == null) {
					throw CeremonyErrorCode.FAMILY_RELATION_REQUIRED.toBaseException();
				}
			}
			case INSTEAD -> {
				if (alumniRelation == null) {
					throw CeremonyErrorCode.ALUMNI_RELATION_REQUIRED.toBaseException();
				}
				if (alumniName == null) {
					throw CeremonyErrorCode.ALUMNI_NAME_REQUIRED.toBaseException();
				}
				if (alumniAdmissionYear == null) {
					throw CeremonyErrorCode.ALUMNI_ADMISSION_YEAR_REQUIRED.toBaseException();
				}
			}
		}
	}
}
