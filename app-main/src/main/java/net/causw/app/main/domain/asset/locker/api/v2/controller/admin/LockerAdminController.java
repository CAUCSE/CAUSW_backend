package net.causw.app.main.domain.asset.locker.api.v2.controller.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/lockers")
public class LockerAdminController {

	@GetMapping("/schedules/current")
	public ApiResponse<Void> getCurrentLockerSchedule() {

		if (true) {
			throw new BaseRunTimeV2Exception(AuthErrorCode.INVALID_TOKEN);
		}
		return ApiResponse.success();
	}

}
