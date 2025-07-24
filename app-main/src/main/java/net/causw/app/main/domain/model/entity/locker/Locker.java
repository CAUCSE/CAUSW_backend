package net.causw.app.main.domain.model.entity.locker;

import java.time.LocalDateTime;
import java.util.Optional;

import org.hibernate.annotations.ColumnDefault;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.user.User;

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

	public static Locker of(
		Long lockerNumber,
		Boolean isActive,
		User user,
		LockerLocation location,
		LocalDateTime expireDate
	) {
		return Locker.builder()
			.lockerNumber(lockerNumber)
			.isActive(isActive)
			.user(user)
			.location(location)
			.expireDate(expireDate)
			.build();
	}

	public Optional<User> getUser() {
		return Optional.ofNullable(this.user);
	}

	public void update(boolean isActive, User user, LocalDateTime expireDate) {
		this.isActive = isActive;
		this.user = user;
		this.expireDate = expireDate;
	}

	public void updateLocation(LockerLocation location) {
		this.location = location;
	}

	public void register(User user, LocalDateTime expiredAt) {
		this.user = user;
		this.isActive = Boolean.FALSE;
		this.expireDate = expiredAt;
	}

	public void returnLocker() {
		this.user = null;
		this.isActive = Boolean.TRUE;
		this.expireDate = null;
	}

	public void extendExpireDate(LocalDateTime expiredAt) {
		this.expireDate = expiredAt;
	}

	public void activate() {
		this.isActive = true;
	}

	public void deactivate() {
		this.isActive = false;
		this.user = null;
	}

	public void move(LockerLocation lockerLocation) {
		this.location = lockerLocation;
	}

}
