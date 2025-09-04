package net.causw.app.main.domain.model.entity.flag;

import org.hibernate.annotations.ColumnDefault;

import net.causw.app.main.domain.model.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "TB_FLAG")
public class Flag extends BaseEntity {
	@Column(name = "tb_key", unique = true, nullable = false)
	private String key;

	@Column(name = "value")
	@ColumnDefault("false")
	private Boolean value;

	public static Flag of(
		String key,
		Boolean value
	) {
		return Flag.builder()
			.key(key)
			.value(value)
			.build();
	}
}
