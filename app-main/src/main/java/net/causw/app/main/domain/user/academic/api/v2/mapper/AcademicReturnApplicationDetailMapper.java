package net.causw.app.main.domain.user.academic.api.v2.mapper;

import net.causw.app.main.domain.user.academic.api.v2.dto.response.AcademicReturnApplicationDetailResponse;
import net.causw.app.main.domain.user.academic.service.dto.response.AcademicReturnApplicationDetailResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AcademicReturnApplicationDetailMapper {

	// Result → Response
	AcademicReturnApplicationDetailResponse toResponse(AcademicReturnApplicationDetailResult result);
}
