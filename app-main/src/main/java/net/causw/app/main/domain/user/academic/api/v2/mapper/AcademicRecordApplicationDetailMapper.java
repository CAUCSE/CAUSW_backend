package net.causw.app.main.domain.user.academic.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.user.academic.api.v2.dto.response.AcademicRecordApplicationDetailResponse;
import net.causw.app.main.domain.user.academic.service.dto.response.AcademicRecordApplicationDetailResult;

@Mapper(componentModel = "spring")
public interface AcademicRecordApplicationDetailMapper {

	// Result → Response
	AcademicRecordApplicationDetailResponse toResponse(AcademicRecordApplicationDetailResult result);
}
