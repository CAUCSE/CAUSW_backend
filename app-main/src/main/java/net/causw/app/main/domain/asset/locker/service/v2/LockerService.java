package net.causw.app.main.domain.asset.locker.service.v2;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerLocation;
import net.causw.app.main.domain.asset.locker.repository.dto.LockerCountByLocation;
import net.causw.app.main.domain.asset.locker.service.v2.dto.result.LockerFloorListResult;
import net.causw.app.main.domain.asset.locker.service.v2.dto.result.LockerLocationResult;
import net.causw.app.main.domain.asset.locker.service.v2.dto.result.LockerPeriodStatusResult;
import net.causw.app.main.domain.asset.locker.service.v2.dto.result.MyLockerResult;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerLocationReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerLogWriter;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerPeriodResolver;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerPolicyReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerReader;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerValidator;
import net.causw.app.main.domain.asset.locker.service.v2.implementation.LockerMapper;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;

import lombok.RequiredArgsConstructor;

/**
 * 일반 유저용 사물함 서비스.
 *
 * <p>사물함 신청·반납·연장과 조회 기능을 제공한다.
 * 정책(신청/연장 기간) 검증은 {@link LockerValidator}에 위임하고,
 * 조회 결과 매핑은 {@link LockerMapper}에 위임한다.</p>
 *
 * @see LockerAdminService 관리자용 사물함 서비스
 * @see LockerPolicyAdminService 사물함 정책 관리 서비스
 */
@Service
@RequiredArgsConstructor
public class LockerService {

	private final LockerReader lockerReader;
	private final LockerLocationReader lockerLocationReader;
	private final LockerPolicyReader lockerPolicyReader;
	private final LockerPeriodResolver lockerPeriodResolver;
	private final LockerLogWriter lockerLogWriter;
	private final LockerValidator lockerValidator;
	private final UserReader userReader;

	/**
	 * 사물함 신청 (일반 유저용)
	 * 1. 사물함 신청 기간 검증
	 * 2. 사물함 상태 검증 (비어있고, 활성화된 상태)
	 * 3. 기존 사물함 보유 시 자동 반납
	 * 4. 글로벌 만료일(EXPIRE_DATE) 기반으로 신청
	 *
	 * @param lockerId 신청할 사물함 ID
	 * @param userId 신청 유저 아이디
	 */
	@Transactional
	public void registerLocker(String lockerId, String userId) {
		User user = userReader.findUserById(userId);
		lockerValidator.validateRegisterPeriod(LocalDateTime.now());

		// 사물함 상태 검증
		Locker locker = lockerReader.findByIdForWrite(lockerId);
		lockerValidator.validateRegisterAvailable(locker);

		// 기존 사물함 보유 시 자동 반납
		lockerReader.findByUserId(user.getId()).ifPresent(existingLocker -> {
			existingLocker.returnLocker();
			lockerLogWriter.logReturn(existingLocker, user);
		});

		// 사물함 신청
		locker.register(user, lockerPolicyReader.findExpireDate());
		lockerLogWriter.logRegister(locker, user);
	}

	/**
	 * 사물함 반납 (일반 유저용)
	 * 1. 사물함 신청 기간 검증
	 * 2. 사물함 사용중 상태 검증
	 * 3. 소유자 검증
	 * 4. 반납
	 *
	 * @param lockerId 반납할 사물함 ID
	 * @param userId 반납 유저 아이디
	 */
	@Transactional
	public void returnLocker(String lockerId, String userId) {
		// 사물함 신청 기간 검증
		User user = userReader.findUserById(userId);
		lockerValidator.validateReturnPeriod(LocalDateTime.now());

		// 사물함 사용중 및 보유 상태 검증
		Locker locker = lockerReader.findByIdForWrite(lockerId);
		lockerValidator.validateInUse(locker);
		lockerValidator.validateOwner(locker, user);

		// 사물함 반납
		locker.returnLocker();
		lockerLogWriter.logReturn(locker, user);
	}

	/**
	 * 사물함 연장 (일반 유저용)
	 * 1. 연장 가능 상태 및 연장기간 확인
	 * 2. 사물함 사용중 상태 검증
	 * 3. 소유자 검증
	 * 4. 이미 연장 여부 검증
	 * 5. NEXT_EXPIRED_AT 기반으로 연장
	 *
	 * @param lockerId 연장할 사물함 ID
	 * @param userId 연장 유저 아이디
	 */
	@Transactional
	public void extendLocker(String lockerId, String userId) {
		User user = userReader.findUserById(userId);
		// 현재 연장가능기간인지 확인
		lockerValidator.validateExtendPeriod(LocalDateTime.now());

		// 선택한 사물함에 대해서 사용중인지, 본인이 소유중인지 검증
		Locker locker = lockerReader.findByIdForWrite(lockerId);
		lockerValidator.validateInUse(locker);
		lockerValidator.validateOwner(locker, user);

		LocalDateTime nextExpireDate = lockerPolicyReader.findNextExpireDate();
		lockerValidator.validateNotAlreadyExtended(locker, nextExpireDate);

		locker.extendExpireDate(nextExpireDate);
		lockerLogWriter.logExtend(locker, user);
	}

	/**
	 * 현재 사물함 기간 정책 상태를 조회한다.
	 *
	 * @return 현재 phase와 해당 기간의 시작/종료 일시
	 */
	@Transactional(readOnly = true)
	public LockerPeriodStatusResult findCurrentPeriodStatus() {
		return lockerPeriodResolver.resolveCurrentPhase(LocalDateTime.now());
	}

	/**
	 * 전체 층의 사물함 요약 정보를 조회한다.
	 *
	 * @return 전체 집계 및 층별 사물함 요약 목록
	 */
	@Transactional(readOnly = true)
	public LockerFloorListResult findAllFloors() {
		// 전체 층 조회
		List<LockerLocation> locations = lockerLocationReader.findAll();
		// 전체 층 집계 정보 조회(전체 개수, 사용가능 개수)
		Map<String, LockerCountByLocation> countsByLocation = lockerReader.countGroupByLocation();
		LockerCountByLocation empty = new LockerCountByLocation("", 0, 0);

		// 층별 집계정보 분배
		List<LockerFloorListResult.FloorItemResult> floorItems = locations.stream()
			.map(location -> {
				LockerCountByLocation counts = countsByLocation.getOrDefault(location.getId(), empty);
				return LockerMapper.toFloorItemResult(location, counts.totalCount(), counts.availableCount());
			})
			.toList();

		return LockerMapper.toFloorListResult(floorItems);
	}

	/**
	 * 현재 유저의 사물함 정보를 조회한다.
	 *
	 * @param userId 조회 유저 ID
	 * @return 사물함 보유 시 상세 정보, 미보유 시 빈 결과
	 */
	@Transactional(readOnly = true)
	public MyLockerResult findMyLocker(String userId) {
		var user = userReader.findUserById(userId);

		return lockerReader.findByUserId(user.getId())
			.map(LockerMapper::toMyLockerResult)
			.orElse(MyLockerResult.empty());
	}

	/**
	 * 특정 층의 사물함 목록을 조회한다.
	 *
	 * <p>각 사물함의 상태를 유저 기준으로 계산하며({@code MINE} 포함),
	 * 현재 정책 상태와 유저 가능 액션 정보를 함께 반환한다.</p>
	 *
	 * @param locationId 층 위치 ID
	 * @param userId 조회 유저 ID
	 * @return 층 정보, 정책, 집계, 사물함 목록을 포함한 결과
	 */
	@Transactional(readOnly = true)
	public LockerLocationResult findByLocation(String locationId, String userId) {
		LockerLocation location = lockerLocationReader.findById(locationId);
		List<Locker> lockers = lockerReader.findByLocationIdWithUser(locationId);

		boolean canApplyPolicy = lockerPeriodResolver.isRegisterActive(LocalDateTime.now());
		boolean canExtendPolicy = lockerPeriodResolver.isExtendActive(LocalDateTime.now());

		List<LockerLocationResult.LockerItemResult> lockerItems = LockerMapper.toLockerItemResults(lockers, userId);

		return LockerMapper.toLocationResult(location, lockers, lockerItems, canApplyPolicy, canExtendPolicy);
	}
}
