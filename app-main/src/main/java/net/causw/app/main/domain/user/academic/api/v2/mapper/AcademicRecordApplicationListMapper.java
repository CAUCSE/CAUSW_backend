package net.causw.app.main.domain.user.academic.api.v2.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.user.academic.api.v2.dto.request.AcademicRecordApplicationListRequest;
import net.causw.app.main.domain.user.academic.api.v2.dto.response.AcademicRecordApplicationSummaryResponse;
import net.causw.app.main.domain.user.academic.service.dto.request.AcademicRecordApplicationListCondition;
import net.causw.app.main.domain.user.academic.service.dto.response.AcademicRecordApplicationSummaryResult;

@Mapper(componentModel = "spring")
public interface AcademicRecordApplicationListMapper {

	// Request → Condition
	@Mapping(target = "page", expression = "java(request.page() != null ? request.page() : 0)")
	@Mapping(target = "size", expression = "java(request.size() != null ? request.size() : 10)")
	AcademicRecordApplicationListCondition toCondition(AcademicRecordApplicationListRequest request);

	// Result → Response
	AcademicRecordApplicationSummaryResponse toResponse(AcademicRecordApplicationSummaryResult result);
}
