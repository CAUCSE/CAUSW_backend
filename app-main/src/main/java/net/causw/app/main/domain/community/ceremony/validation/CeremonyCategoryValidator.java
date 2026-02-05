package net.causw.app.main.domain.community.ceremony.validation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.ceremony.enums.CeremonyCategory;
import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;

@Component
public class CeremonyCategoryValidator {

	public void validateCategory(CeremonyCategory ceremonyCategory, String ceremonyCustomCategory) {
		if (ceremonyCategory == CeremonyCategory.ETC) {
			if (ceremonyCustomCategory == null
				|| ceremonyCustomCategory.isBlank()) {
				throw CeremonyErrorCode.CUSTOM_CATEGORY_REQUIRED.toBaseException();
			}
		}
	}
}