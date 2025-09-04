package net.causw.app.main.dto.locker;

import java.time.LocalDateTime;
import java.util.Optional;

import net.causw.app.main.domain.model.entity.locker.Locker;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.global.constant.StaticValue;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LockerResponseDto {
	private String id;
	private String lockerNumber;
	private Boolean isActive;
	private Boolean isMine;
	private String expireAt;
	private LocalDateTime updatedAt;

	public static LockerResponseDto of(Locker locker, User user) {
		return LockerResponseDto.builder()
			.id(locker.getId())
			.lockerNumber(String.valueOf(locker.getLockerNumber()))
			.isActive(locker.getIsActive())
			.isMine(locker.getUser().map(User::getId).orElse("").equals(user.getId()))
			.expireAt(Optional.ofNullable(locker.getExpireDate()).map(
				expire -> expire.format(StaticValue.LOCKER_DATE_TIME_FORMATTER)).orElse(null))
			.updatedAt(locker.getUpdatedAt())
			.build();
	}

	public static LockerResponseDto of(
		Locker locker,
		User user,
		String locationName
	) {
		String location = locationName + " " + locker.getLockerNumber();

		return LockerResponseDto.builder()
			.id(locker.getId())
			.lockerNumber(location)
			.isActive(locker.getIsActive())
			.isMine(locker.getUser().map(User::getId).orElse("").equals(user.getId()))
			.expireAt(Optional.ofNullable(locker.getExpireDate()).map(
				expire -> expire.format(StaticValue.LOCKER_DATE_TIME_FORMATTER)).orElse(null))
			.updatedAt(locker.getUpdatedAt())
			.build();
	}
}
