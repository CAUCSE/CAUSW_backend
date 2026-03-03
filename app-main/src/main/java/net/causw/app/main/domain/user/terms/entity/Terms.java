package net.causw.app.main.domain.user.terms.entity;

import java.time.LocalDate;

import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
	name = "tb_terms",
	uniqueConstraints = @UniqueConstraint(columnNames = {"type", "version"}))
public class Terms extends BaseEntity {

	@Column(name = "title", nullable = false)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private TermsType type;

	@Column(name = "is_required", nullable = false)
	private boolean isRequired;

	@Column(name = "version", nullable = false)
	private int version;

	@Column(name = "effective_date", nullable = false)
	private LocalDate effectiveDate;

	@Lob
	@Column(columnDefinition = "TEXT", name = "content", nullable = false)
	private String content;
}
