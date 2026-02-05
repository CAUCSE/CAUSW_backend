package net.causw.app.main.domain.user.academic.api.v2.mapper;

import net.causw.app.main.domain.user.academic.api.v2.dto.request.AcademicReturnApplicationListRequest;
import net.causw.app.main.domain.user.academic.api.v2.dto.response.AcademicReturnApplicationSummaryResponse;
import net.causw.app.main.domain.user.academic.service.dto.request.AcademicReturnApplicationListCondition;
import net.causw.app.main.domain.user.academic.service.dto.result.AcademicReturnApplicationSummaryResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AcademicReturnApplicationMapper {

	// Request → Condition
	@Mapping(target = "page", expression = "java(request.page() != null ? request.page() : 0)")
	@Mapping(target = "size", expression = "java(request.size() != null ? request.size() : 10)")
	AcademicReturnApplicationListCondition toCondition(AcademicReturnApplicationListRequest request);

	// Result → Response
	AcademicReturnApplicationSummaryResponse toResponse(AcademicReturnApplicationSummaryResult result);
}
