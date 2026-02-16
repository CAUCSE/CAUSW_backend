package net.causw.app.main.domain.asset.locker.service.v2;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.api.v2.controller.admin.dto.response.LockerPolicyResponse;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerPolicyReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerPolicyWriter;

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
	public void updateExpireDate(LocalDateTime expiredAt) {
		lockerPolicyWriter.updateExpireDate(expiredAt);
	}

	@Transactional
	public void updateRegisterPeriod(LocalDateTime start, LocalDateTime end) {
		lockerPolicyWriter.updateRegisterPeriod(start, end);
	}

	@Transactional
	public void updateExtendPeriod(LocalDateTime start, LocalDateTime end, LocalDateTime nextExpireDate) {
		lockerPolicyWriter.updateExtendPeriod(start, end, nextExpireDate);
	}
}
