package net.causw.app.main.domain.user.account.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.user.account.api.v2.dto.response.UserDropResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserRestoreResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserRoleUpdateResponse;
import net.causw.app.main.domain.user.account.service.dto.response.UserDropResult;
import net.causw.app.main.domain.user.account.service.dto.response.UserRestoreResult;
import net.causw.app.main.domain.user.account.service.dto.response.UserRoleUpdateResult;

@Mapper(componentModel = "spring")
public interface UserManagementMapper {

	UserDropResponse toDropResponse(UserDropResult result);

	UserRestoreResponse toRestoreResponse(UserRestoreResult result);

	UserRoleUpdateResponse toRoleUpdateResponse(UserRoleUpdateResult result);
}
