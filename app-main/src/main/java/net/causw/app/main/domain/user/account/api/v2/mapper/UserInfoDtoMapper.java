package net.causw.app.main.domain.user.account.api.v2.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.user.account.api.v2.dto.request.UserInfoListRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserInfoUpdateRequest;
import net.causw.app.main.domain.user.account.api.v2.dto.response.DepartmentResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoDetailResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoDirectoryResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoSectionResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoSummaryResponse;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoListCondition;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoUpdateCommand;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoDetailResult;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoDirectoryResult;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoSectionResult;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoSummaryResult;

@Mapper(componentModel = "spring")
public interface UserInfoDtoMapper {

	@Mapping(target = "departmentDescription", source = "result.department.name", defaultValue = "학과 미기입")
	UserInfoDetailResponse toDetailResponse(UserInfoDetailResult result);

	UserInfoSummaryResponse toSummaryResponse(UserInfoSummaryResult result);

	default DepartmentResponse toDepartmentResponse(Department department) {
		if (department == null) {
			return null;
		}
		return new DepartmentResponse(department.name(), department.getName());
	}

	UserInfoSectionResponse toSectionResponse(UserInfoSectionResult result);

	UserInfoDirectoryResponse toDirectoryResponse(UserInfoDirectoryResult result);

	UserInfoUpdateCommand toUpdateCommand(UserInfoUpdateRequest request);

	UserInfoListCondition toListCondition(UserInfoListRequest request);
}
