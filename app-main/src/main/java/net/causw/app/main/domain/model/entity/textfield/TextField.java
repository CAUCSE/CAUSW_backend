package net.causw.app.main.domain.model.entity.textfield;

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
@Table(name = "TB_TEXT_FIELD")
public class TextField extends BaseEntity {
	@Column(name = "tb_key", unique = true, nullable = false)
	private String key;

	@Column(name = "value", nullable = false)
	private String value;

	public static TextField of(
		String key,
		String value
	) {
		return TextField.builder()
			.key(key)
			.value(value)
			.build();
	}
}
