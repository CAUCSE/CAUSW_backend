package net.causw.app.main.domain.community.ceremony.util;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.ceremony.enums.CeremonyCategory;
import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;
import net.causw.app.main.shared.exception.errorcode.GlobalErrorCode;

@Component
public class CeremonyCategoryValidator {

	public void validateCustomCategory(CeremonyCategory ceremonyCategory, String ceremonyCustomCategory) {
		if (ceremonyCategory == CeremonyCategory.ETC) {
			if (ceremonyCustomCategory == null || ceremonyCustomCategory.isBlank()) {
				throw CeremonyErrorCode.CUSTOM_CATEGORY_REQUIRED.toBaseException();
			}
		} else {
			if (ceremonyCustomCategory != null) {
				throw GlobalErrorCode.BAD_REQUEST.toBaseException();
			}
		}
	}
}