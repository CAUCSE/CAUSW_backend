package net.causw.app.main.domain.user.academic.api.v2.mapper;

import net.causw.app.main.domain.user.academic.api.v2.dto.response.AcademicRecordApplicationDetailResponse;
import net.causw.app.main.domain.user.academic.service.dto.response.AcademicRecordApplicationDetailResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AcademicRecordApplicationDetailMapper {

	// Result → Response
	AcademicRecordApplicationDetailResponse toResponse(AcademicRecordApplicationDetailResult result);
}
