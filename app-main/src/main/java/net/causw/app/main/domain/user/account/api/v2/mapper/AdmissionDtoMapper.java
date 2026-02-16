package net.causw.app.main.domain.user.account.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.user.account.api.v2.dto.request.AdmissionCreateRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.AdmissionListRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.response.AdmissionListItemResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.AdmissionResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.AdmissionStateResponse;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionCreateCommand;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionListCondition;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionResult;
import net.causw.app.main.domain.user.account.service.v2.dto.AdmissionStateResult;

@Mapper(componentModel = "spring")
public interface AdmissionDtoMapper {

	// ── 유저 플로우 ──

	AdmissionCreateCommand toCreateCommand(AdmissionCreateRequest request);

	AdmissionResponse toResponse(AdmissionResult dto);

	AdmissionStateResponse toStateResponse(AdmissionStateResult dto);

	// ── 관리자 플로우 ──

	AdmissionListCondition toCondition(AdmissionListRequest request);

	AdmissionListItemResponse toListItemResponse(AdmissionResult result);
}
