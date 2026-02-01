package net.causw.app.main.shared.entity;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(value = {AuditingEntityListener.class})
public class BaseEntity extends AuditableEntity {

	@Id
	@UuidGenerator
	@Column(name = "id", nullable = false, unique = true)
	private String id;

	protected BaseEntity(String id) {
		this.id = id;
	}
}
