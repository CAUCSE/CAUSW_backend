package net.causw.app.main.domain.model.entity.uuidFile.joinEntity;

import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_user_profile_uuid_file",
	indexes = {
		@Index(name = "idx_user_profile_user_id", columnList = "user_id"),
		@Index(name = "idx_user_profile_uuid_file_id", columnList = "uuid_file_id")
	})
public class UserProfileImage extends JoinEntity {

	@Setter(AccessLevel.PUBLIC)
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "uuid_file_id", nullable = false, unique = true)
	public UuidFile uuidFile;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	public static UserProfileImage of(User user, UuidFile uuidFile) {
		return UserProfileImage.builder()
			.user(user)
			.uuidFile(uuidFile)
			.build();
	}

}
