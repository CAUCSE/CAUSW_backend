package net.causw.app.main.domain.user.account.entity.userInfo;

import java.util.ArrayList;
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

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

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

	public void update(
		String description,
		String job,
		List<String> socialLinks,
		boolean isPhoneNumberVisible) {
		if (socialLinks.size() > 10) {
			throw UserInfoErrorCode.TOO_MUCH_SOCIAL_LINK.toBaseException();
		}

		this.description = description;
		this.job = job;
		this.socialLinks = socialLinks;
		this.isPhoneNumberVisible = isPhoneNumberVisible;
	}
}
