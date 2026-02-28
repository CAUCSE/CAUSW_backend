package net.causw.app.main.domain.community.ceremony.api.v2.mapper;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.ceremony.api.v2.dto.request.CeremonyAdminListRequest;
import net.causw.app.main.domain.community.ceremony.service.dto.request.CeremonyAdminListCondition;

@Component
public class CeremonyAdminListMapper {

	public CeremonyAdminListCondition toCondition(CeremonyAdminListRequest request) {
		return new CeremonyAdminListCondition(
			request.fromDate(),
			request.toDate(),
			request.state());
	}
}
