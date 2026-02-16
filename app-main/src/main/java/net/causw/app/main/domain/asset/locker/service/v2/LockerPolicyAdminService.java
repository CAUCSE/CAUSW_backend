package net.causw.app.main.domain.asset.locker.service.v2;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.response.LockerPolicyResponse;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerPolicyReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerPolicyWriter;
import net.causw.app.main.shared.exception.errorcode.LockerErrorCode;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LockerPolicyAdminService {

	private final LockerPolicyReader lockerPolicyReader;
	private final LockerPolicyWriter lockerPolicyWriter;

	@Transactional(readOnly = true)
	public LockerPolicyResponse getPolicy() {
		return new LockerPolicyResponse(
			lockerPolicyReader.findExpireDateOptional().orElse(null),
			lockerPolicyReader.findRegisterStartDate().orElse(null),
			lockerPolicyReader.findRegisterEndDate().orElse(null),
			lockerPolicyReader.findExtendStartDate().orElse(null),
			lockerPolicyReader.findExtendEndDate().orElse(null),
			lockerPolicyReader.findNextExpireDateOptional().orElse(null),
			lockerPolicyReader.isRegisterPeriod(),
			lockerPolicyReader.isExtendPeriod());
	}

	@Transactional
	public void updateRegisterPeriod(LocalDateTime start, LocalDateTime end, LocalDateTime expiredAt) {
		lockerPolicyWriter.updateRegisterPeriod(start, end, expiredAt);
	}

	@Transactional
	public void updateExtendPeriod(LocalDateTime start, LocalDateTime end, LocalDateTime nextExpireDate) {
		lockerPolicyWriter.updateExtendPeriod(start, end, nextExpireDate);
	}

	@Transactional
	public void updateRegisterStatus(@NotNull boolean status) {
		if (status && lockerPolicyReader.isExtendPeriod()) {
			throw LockerErrorCode.LOCKER_EXTEND_ALREADY_ACTIVE.toBaseException();
		}
		lockerPolicyWriter.updateRegisterStatus(status);
	}

	@Transactional
	public void updateExtendStatus(@NotNull boolean status) {
		if (status && lockerPolicyReader.isRegisterPeriod()) {
			throw LockerErrorCode.LOCKER_REGISTER_ALREADY_ACTIVE.toBaseException();
		}
		lockerPolicyWriter.updateExtendStatus(status);
	}
}
