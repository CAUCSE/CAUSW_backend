package net.causw.app.main.domain.asset.locker.api.v2.controller.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.core.global.annotation.V2Api;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@V2Api
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/lockers")
public class LockerAdminController {

	@GetMapping("/schedules/current")
	public ApiResponse<Void> getCurrentLockerSchedule() {

		if (true) {
			throw new BadRequestException(
				ErrorCode.INVALID_EXPIRE_DATE,
				MessageUtil.LOCKER_ALREADY_EXTENDED);
		}
		return ApiResponse.ofDefault();
	}

}
