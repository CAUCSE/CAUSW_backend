package net.causw.app.main.domain.user.account.entity.userInfo;

import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user_project")
public class UserProject extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_info_id", nullable = false)
	private UserInfo userInfo;

	@Column(name = "start_year", nullable = false)
	private Integer startYear;

	@Column(name = "start_month", nullable = false)
	private Integer startMonth;

	@Column(name = "end_year", nullable = true)
	private Integer endYear;

	@Column(name = "end_month", nullable = true)
	private Integer endMonth;

	@Column(name = "description", nullable = false)
	private String description;

	public static UserProject of(
		UserInfo userInfo,
		Integer startYear, Integer startMonth,
		Integer endYear, Integer endMonth,
		String description) {
		return UserProject.builder()
			.userInfo(userInfo)
			.startYear(startYear)
			.startMonth(startMonth)
			.endYear(endYear)
			.endMonth(endMonth)
			.description(description)
			.build();
	}

	public void update(
		Integer startYear, Integer startMonth,
		Integer endYear, Integer endMonth,
		String description) {
		this.startYear = startYear;
		this.startMonth = startMonth;
		this.endYear = endYear;
		this.endMonth = endMonth;
		this.description = description;
	}
}
