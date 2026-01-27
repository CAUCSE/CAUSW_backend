package net.causw.app.main.domain.asset.locker.api.v2.controller.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.core.global.annotation.V2Api;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/lockers")
@V2Api
public class LockerAdminController {

	@GetMapping("/schedules/current")
	public String getCurrentLockerSchedule() {

		if (true) {
			throw new BadRequestException(
				ErrorCode.INVALID_EXPIRE_DATE,
				MessageUtil.LOCKER_ALREADY_EXTENDED);
		}
		return "test";
	}

}
