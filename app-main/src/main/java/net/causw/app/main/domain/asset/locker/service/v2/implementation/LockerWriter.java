package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.repository.LockerRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LockerWriter {

	private final LockerRepository lockerRepository;
	private final LockerLogWriter lockerLogWriter;

	// 일반 유저 사물함 반납
	public void returnLocker(Locker locker, User user) {
		locker.returnLocker();
		lockerRepository.save(locker);
		lockerLogWriter.logReturn(locker, user);
	}

	// 관리자 사물함 회수
	public void releaseLocker(Locker locker, User admin, String userEmail, String userName) {
		locker.returnLocker();
		lockerRepository.save(locker);
		lockerLogWriter.logAdminRelease(locker, admin, userEmail, userName);
	}

	// 일반 유저 사물함 신청
	public void registerLocker(Locker locker, User user, LocalDateTime expiredAt) {
		locker.register(user, expiredAt);
		lockerRepository.save(locker);
		lockerLogWriter.logRegister(locker, user);
	}

	// 관리자 사물함 배정
	public void assignLocker(Locker locker, User admin, User assignee, LocalDateTime expiredAt) {
		locker.register(assignee, expiredAt);
		lockerRepository.save(locker);
		lockerLogWriter.logAdminAssign(locker, admin, assignee);
	}

	// 일반 유저 사물함 연장
	public void extendLocker(Locker locker, User user, LocalDateTime expiredAt) {
		locker.extendExpireDate(expiredAt);
		lockerRepository.save(locker);
		lockerLogWriter.logExtend(locker, user);
	}

	// 관리자 사물함 연장 처리
	public void extendLockerByAdmin(Locker locker, User admin, User assignee, LocalDateTime expiredAt) {
		locker.extendExpireDate(expiredAt);
		lockerRepository.save(locker);
		lockerLogWriter.logAdminExtend(locker, admin, assignee);
	}

	// 관리자 사물함 활성화 처리
	public void enableLocker(Locker locker, User admin) {
		locker.enable();
		lockerRepository.save(locker);
		lockerLogWriter.logEnable(locker, admin);
	}

	// 관리자 사물함 비활성화 처리
	public void disableLocker(Locker locker, User admin) {
		locker.disable();
		lockerRepository.save(locker);
		lockerLogWriter.logDisable(locker, admin);
	}
}
