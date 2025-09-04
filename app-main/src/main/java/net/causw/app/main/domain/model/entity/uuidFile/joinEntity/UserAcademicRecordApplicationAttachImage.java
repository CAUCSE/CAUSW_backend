package net.causw.app.main.domain.model.entity.uuidFile.joinEntity;

import net.causw.app.main.domain.model.entity.userAcademicRecord.UserAcademicRecordApplication;
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
@Table(name = "tb_user_academic_record_application_attach_image_uuid_file",
	indexes = {
		@Index(name = "idx_user_academic_record_application_attach_image_application_id", columnList = "user_academic_record_application_id"),
		@Index(name = "idx_user_academic_record_application_attach_image_uuid_file_id", columnList = "uuid_file_id")
	})
public class UserAcademicRecordApplicationAttachImage extends JoinEntity {

	@Getter
	@Setter(AccessLevel.PUBLIC)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "uuid_file_id", nullable = false)
	public UuidFile uuidFile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_academic_record_application_id", nullable = false)
	private UserAcademicRecordApplication userAcademicRecordApplication;

	public static UserAcademicRecordApplicationAttachImage of(
		UserAcademicRecordApplication userAcademicRecordApplication, UuidFile uuidFile) {
		return UserAcademicRecordApplicationAttachImage.builder()
			.uuidFile(uuidFile)
			.userAcademicRecordApplication(userAcademicRecordApplication)
			.build();
	}

}
