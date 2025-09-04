package net.causw.app.main.domain.model.entity.locker;

import net.causw.app.main.domain.model.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "TB_LOCKER_LOCATION")
public class LockerLocation extends BaseEntity {
	@Enumerated(EnumType.STRING)
	@Column(name = "name", unique = true, nullable = false)
	private LockerName name;

	public static LockerLocation of(LockerName name) {
		return LockerLocation.builder()
			.name(name)
			.build();
	}

	public void update(LockerName name) {
		this.name = name;
	}

	public String getName() {
		return this.name.name();
	}
}
