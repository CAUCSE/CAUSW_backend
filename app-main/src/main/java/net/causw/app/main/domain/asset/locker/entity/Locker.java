package net.causw.app.main.domain.asset.locker.entity;

import java.time.LocalDateTime;
import java.util.Optional;

import org.hibernate.annotations.ColumnDefault;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "TB_LOCKER")
public class Locker extends BaseEntity {
	@Column(name = "locker_number", nullable = false)
	private Long lockerNumber;

	@Column(name = "is_active")
	@ColumnDefault("true")
	private Boolean isActive;

	@Column(name = "expire_date")
	private LocalDateTime expireDate;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = true)
	private User user;

	@ManyToOne
	@JoinColumn(name = "location_id", nullable = false)
	private LockerLocation location;

	// 연장여부 (반납/회수 시 기본값 false)
	private boolean isExtended;

	public static Locker of(
		Long lockerNumber,
		Boolean isActive,
		User user,
		LockerLocation location,
		LocalDateTime expireDate) {
		return Locker.builder()
			.lockerNumber(lockerNumber)
			.isActive(isActive)
			.user(user)
			.location(location)
			.expireDate(expireDate)
			.isExtended(false)
			.build();
	}

	public Optional<User> getUser() {
		return Optional.ofNullable(this.user);
	}

	/**
	 * 사물함 신청
	 * @param user 신청 유저
	 * @param expiredAt 만료일
	 */
	public void register(User user, LocalDateTime expiredAt) {
		this.user = user;
		this.expireDate = expiredAt;
		this.isExtended = false;
	}

	/**
	 * 사물함 반납
	 */
	public void returnLocker() {
		this.user = null;
		this.expireDate = null;
		this.isExtended = false;
	}

	/**
	 * 사물함 연장
	 * @param expiredAt 연장 일시
	 */
	public void extendExpireDate(LocalDateTime expiredAt) {
		this.expireDate = expiredAt;
		this.isExtended = true;
	}

	/**
	 * 사물함 활성화
	 */
	public void enable() {
		this.isActive = true;
	}

	/**
	 * 사물함 비활성화
	 */
	public void disable() {
		this.isActive = false;
		this.user = null;
	}

	public LockerStatus getStatus() {
		return LockerStatus.of(this);
	}

	public LockerStatus getStatus(String userId) {
		return LockerStatus.of(this, userId);
	}

	public boolean isEnabled() {
		return Boolean.TRUE.equals(this.isActive);
	}

}
