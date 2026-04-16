package net.causw.app.main.domain.user.account.entity.userInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.entity.BaseEntity;
import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user_info")
public class UserInfo extends BaseEntity {

	private static final int MAX_SOCIAL_LINKS = 10;
	private static final int MAX_TECH_STACK = 10;
	private static final int MAX_CAREER = 10;
	private static final int MAX_PROJECT = 10;
	private static final int MAX_INTEREST_TECH = 10;
	private static final int MAX_INTEREST_DOMAIN = 10;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	/**
	 * @deprecated v2 API에서는 직업(job) 정보가 소개글(description)과 역할이 겹쳐 더 이상 사용하지 않습니다.
	 * v1 호환을 위해 컬럼은 유지합니다.
	 * TODO: v2 배포 완료 후 제거
	 */
	@Deprecated(since = "v2")
	@Column(name = "job", nullable = true)
	private String job;

	@Column(name = "description", nullable = true)
	private String description;

	@Column(name = "is_phone_number_visible", nullable = false)
	@Builder.Default
	private boolean isPhoneNumberVisible = false;

	@Column(name = "social_links", columnDefinition = "TEXT")
	@Convert(converter = SocialLinksConverter.class)
	@Builder.Default
	private List<String> socialLinks = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "tb_user_tech_stack", joinColumns = @JoinColumn(name = "user_info_id"))
	@Column(name = "tech_stack")
	@Builder.Default
	private Set<String> userTechStack = new HashSet<>();

	@OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true, mappedBy = "userInfo", fetch = FetchType.LAZY)
	@Builder.Default
	private List<UserCareer> userCareer = new ArrayList<>();

	@OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true, mappedBy = "userInfo", fetch = FetchType.LAZY)
	@Builder.Default
	private List<UserProject> userProject = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "tb_user_interest_tech", joinColumns = @JoinColumn(name = "user_info_id"))
	@Column(name = "interest_tech")
	@Builder.Default
	private Set<String> userInterestTech = new HashSet<>();

	@ElementCollection
	@CollectionTable(name = "tb_user_interest_domain", joinColumns = @JoinColumn(name = "user_info_id"))
	@Column(name = "interest_domain")
	@Builder.Default
	private Set<String> userInterestDomain = new HashSet<>();

	public static UserInfo of(User user) {
		return UserInfo.builder()
			.user(user)
			.build();
	}

	/**
	 * v1 API용 업데이트. description, job, socialLinks, isPhoneNumberVisible을 한 번에 갱신한다.
	 * v1은 API 레이어에서 검증하므로 엔티티 레벨 검증 없이 직접 반영한다.
	 */
	@Deprecated(since = "v2 API에서는 description, socialLinks, isPhoneNumberVisible을 각각 업데이트하는 별도의 메서드를 사용합니다.")
	public void updateV1(String description, String job, List<String> socialLinks, boolean isPhoneNumberVisible) {
		this.description = description;
		this.job = job;
		this.socialLinks = socialLinks == null ? new ArrayList<>() : new ArrayList<>(socialLinks);
		this.isPhoneNumberVisible = isPhoneNumberVisible;
	}

	public void update(
		String description,
		boolean isPhoneNumberVisible) {
		this.description = description;
		this.isPhoneNumberVisible = isPhoneNumberVisible;
	}

	public void updateSocialLinks(List<String> incoming) {
		if (incoming != null && incoming.size() > MAX_SOCIAL_LINKS) {
			throw UserInfoErrorCode.TOO_MUCH_SOCIAL_LINK.toBaseException();
		}
		replaceWithFiltered(this.socialLinks, incoming);
	}

	public void updateTechStack(List<String> incoming) {
		if (incoming != null && incoming.size() > MAX_TECH_STACK) {
			throw UserInfoErrorCode.TOO_MUCH_TECH_STACK.toBaseException();
		}
		replaceWithFiltered(this.userTechStack, incoming);
	}

	public void updateInterestTech(List<String> incoming) {
		if (incoming != null && incoming.size() > MAX_INTEREST_TECH) {
			throw UserInfoErrorCode.TOO_MUCH_INTEREST_TECH.toBaseException();
		}
		replaceWithFiltered(this.userInterestTech, incoming);
	}

	public void updateInterestDomain(List<String> incoming) {
		if (incoming != null && incoming.size() > MAX_INTEREST_DOMAIN) {
			throw UserInfoErrorCode.TOO_MUCH_INTEREST_DOMAIN.toBaseException();
		}
		replaceWithFiltered(this.userInterestDomain, incoming);
	}

	public void validateCareerCount(int size) {
		if (size > MAX_CAREER) {
			throw UserInfoErrorCode.TOO_MUCH_CAREER.toBaseException();
		}
	}

	public void validateProjectCount(int size) {
		if (size > MAX_PROJECT) {
			throw UserInfoErrorCode.TOO_MUCH_PROJECT.toBaseException();
		}
	}

	private void replaceWithFiltered(Collection<String> target, List<String> incoming) {
		target.clear();
		if (incoming != null) {
			incoming.stream()
				.filter(s -> s != null && !s.isBlank())
				.forEach(target::add);
		}
	}
}
