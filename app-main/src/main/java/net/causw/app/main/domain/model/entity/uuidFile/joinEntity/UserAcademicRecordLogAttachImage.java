package net.causw.app.main.domain.model.entity.uuidFile.joinEntity;

import net.causw.app.main.domain.model.entity.userAcademicRecord.UserAcademicRecordLog;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "tb_user_academic_record_log_attach_image",
	indexes = {
		@Index(name = "idx_user_academic_record_log_attach_image_log_id", columnList = "user_academic_record_log_id"),
		@Index(name = "idx_user_academic_record_log_attach_image_uuid_file_id", columnList = "uuid_file_id")
	})
public class UserAcademicRecordLogAttachImage extends JoinEntity {

	@Getter
	@Setter(AccessLevel.PUBLIC)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "uuid_file_id", nullable = false, unique = false)
	public UuidFile uuidFile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_academic_record_log_id", nullable = false)
	private UserAcademicRecordLog userAcademicRecordLog;

	public static UserAcademicRecordLogAttachImage of(UserAcademicRecordLog userAcademicRecordLog, UuidFile uuidFile) {
		return UserAcademicRecordLogAttachImage.builder()
			.uuidFile(uuidFile)
			.userAcademicRecordLog(userAcademicRecordLog)
			.build();
	}

}
