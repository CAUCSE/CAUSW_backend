package net.causw.app.main.domain.community.ceremony.util;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.ceremony.enums.RelationType;
import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;
import net.causw.app.main.shared.exception.errorcode.GlobalErrorCode;

@Component
public class CeremonyRelationValidator {

	public void validateRelation(RelationType relationType, String detailRelation,
		String alumniRelation, String alumniName, String alumniAdmissionYear) {
		switch (relationType) {
			case ME -> {
				if (detailRelation != null || alumniRelation != null || alumniName != null
					|| alumniAdmissionYear != null) {
					throw GlobalErrorCode.BAD_REQUEST.toBaseException();
				}
			}
			case FAMILY -> {
				if (detailRelation == null) {
					throw CeremonyErrorCode.FAMILY_RELATION_REQUIRED.toBaseException();
				}
				if (alumniRelation != null || alumniName != null || alumniAdmissionYear != null) {
					throw GlobalErrorCode.BAD_REQUEST.toBaseException();
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
				if (detailRelation != null) {
					throw GlobalErrorCode.BAD_REQUEST.toBaseException();
				}
			}
		}
	}
}
