package net.causw.app.main.domain.user.terms.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.app.main.shared.entity.BaseEntity;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_terms")
public class Terms extends BaseEntity {

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "effective_date", nullable = false)
	private LocalDate effectiveDate;

	@Column(name = "last_revised_date", nullable = false)
	private LocalDate lastRevisedDate;

	@Lob
	@Column(columnDefinition = "TEXT", name = "content", nullable = false)
	private String content;
}
