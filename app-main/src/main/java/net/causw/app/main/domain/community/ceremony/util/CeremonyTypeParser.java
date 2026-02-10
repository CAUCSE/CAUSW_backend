package net.causw.app.main.domain.community.ceremony.util;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.ceremony.enums.CeremonyType;
import net.causw.app.main.shared.exception.errorcode.GlobalErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CeremonyTypeParser {
	public String parseTypeOrNull(String typeParam) {
		if (typeParam == null || typeParam.isEmpty() || typeParam.equalsIgnoreCase("ALL")) {
			return null;
		}

		try {
			return CeremonyType.fromString(typeParam).getValue();
		}
		catch (IllegalArgumentException e) {
			throw GlobalErrorCode.BAD_REQUEST.toBaseException();
		}
	}
}
