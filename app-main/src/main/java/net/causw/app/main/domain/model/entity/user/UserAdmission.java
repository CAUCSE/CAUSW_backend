package net.causw.app.main.domain.model.entity.user;

import java.util.ArrayList;
import java.util.List;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserAdmissionAttachImage;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "tb_user_admission")
public class UserAdmission extends BaseEntity {
	@OneToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, mappedBy = "userAdmission")
	@Builder.Default
	private List<UserAdmissionAttachImage> userAdmissionAttachImageList = new ArrayList<>();

	@Column(name = "description", nullable = true)
	private String description;

	public static UserAdmission of(User requestUser, List<UuidFile> userAdmissionAttachImageUuidFileList,
		String description) {
		UserAdmission userAdmission = UserAdmission.builder()
			.user(requestUser)
			.description(description)
			.build();

		List<UserAdmissionAttachImage> userAdmissionAttachImageList = userAdmissionAttachImageUuidFileList.stream()
			.map(uuidFile -> UserAdmissionAttachImage.of(userAdmission, uuidFile))
			.toList();

		userAdmission.setUserAdmissionAttachImageList(userAdmissionAttachImageList);

		return userAdmission;
	}

	private void setUserAdmissionAttachImageList(List<UserAdmissionAttachImage> userAdmissionAttachImageList) {
		this.userAdmissionAttachImageList = userAdmissionAttachImageList;
	}
}
