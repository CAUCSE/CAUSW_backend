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

	/**
	 * 학과 enum을 API 응답 객체로 변환한다.
	 * @param department 학과 enum
	 * @return 학과 코드와 이름을 포함한 응답, 학과가 없으면 {@code null}
	 */
	default DepartmentResponse toDepartmentResponse(Department department) {
		if (department == null) {
			return null;
		}
		return new DepartmentResponse(department.name(), department.getName());
	}

	/**
	 * 동문 섹션 조회 결과를 API 응답으로 변환한다.
	 * @param result 동문 섹션 조회 결과
	 * @return 동문 섹션 응답
	 */
	UserInfoSectionResponse toSectionResponse(UserInfoSectionResult result);

	/**
	 * 동문 목록 조회 결과를 API 응답으로 변환한다.
	 * @param result 동문 목록 조회 결과
	 * @return 동문 목록 응답
	 */
	UserInfoDirectoryResponse toDirectoryResponse(UserInfoDirectoryResult result);

	UserInfoUpdateCommand toUpdateCommand(UserInfoUpdateRequest request);

	UserInfoListCondition toListCondition(UserInfoListRequest request);
}
