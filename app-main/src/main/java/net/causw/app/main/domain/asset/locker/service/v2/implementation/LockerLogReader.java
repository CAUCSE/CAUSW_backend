package net.causw.app.main.domain.asset.locker.service.v2.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.entity.LockerLog;
import net.causw.app.main.domain.asset.locker.entity.LockerName;
import net.causw.app.main.domain.asset.locker.enums.LockerLogAction;
import net.causw.app.main.domain.asset.locker.repository.query.LockerLogQueryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerLogReader {

	private final LockerLogQueryRepository lockerLogQueryRepository;

	/**
	 * 사물함 액션 로그 리스트 페이지
	 * @param userKeyword 액션 수행 유저 검색 키워드(유저 이메일 or 유저 이름)
	 * @param action 사물함 로그 액션 (활성화, 비활성화, 반납, 연장)
	 * @param lockerLocationName 사물함층 이름 (SECOND, THIRD, FOURTH)
	 * @param lockerNumber 사물함 번호
	 * @param pageable 페이지 객체
	 * @return 사물함 로그 페이지
	 */
	public Page<LockerLog> findLockerLogList(String userKeyword, LockerLogAction action, LockerName lockerLocationName,
		Long lockerNumber, Pageable pageable) {
		return lockerLogQueryRepository.findLockerLogList(userKeyword, action, lockerLocationName, lockerNumber,
			pageable);
	}
}
