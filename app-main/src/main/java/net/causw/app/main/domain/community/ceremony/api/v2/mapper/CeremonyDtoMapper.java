package net.causw.app.main.domain.community.ceremony.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.ceremony.api.v2.dto.request.CeremonyCreateRequest;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonyAdminListResponse;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonyDetailResponse;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonySummaryResponse;
import net.causw.app.main.domain.community.ceremony.service.dto.request.CeremonyCreateCommand;
import net.causw.app.main.domain.community.ceremony.service.dto.response.CeremonyAdminListResult;
import net.causw.app.main.domain.community.ceremony.service.dto.response.CeremonyDetailResult;
import net.causw.app.main.domain.community.ceremony.service.dto.response.CeremonySummaryResult;

@Mapper(componentModel = "spring")
public interface CeremonyDtoMapper {
	CeremonyCreateCommand toCreateCommand(CeremonyCreateRequest request);

	CeremonyDetailResponse toDetailResponse(CeremonyDetailResult result);

	CeremonyDetailResponse toAdminDetailResponse(CeremonyDetailResult result);

	CeremonySummaryResponse toSummaryResponse(CeremonySummaryResult result);

	CeremonyAdminListResponse toAdminListResponse(CeremonyAdminListResult result);
}
