package net.causw.app.main.domain.model.entity.uuidFile;

import org.hibernate.annotations.ColumnDefault;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.enums.uuidFile.FilePath;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_uuid_file")
public class UuidFile extends BaseEntity {

	@Column(name = "uuid", unique = true, nullable = false)
	private String uuid;

	@Column(name = "file_key", unique = true, nullable = false)
	private String fileKey;

	@Lob
	@Column(name = "file_url", columnDefinition = "TEXT", nullable = false)
	private String fileUrl;

	@Column(name = "raw_file_name", nullable = false)
	private String rawFileName;

	@Column(name = "extension", nullable = false)
	private String extension;

	@Enumerated(EnumType.STRING)
	@Column(name = "file_path", nullable = false)
	private FilePath filePath;

	@Setter(AccessLevel.PUBLIC)
	@ColumnDefault("true")
	@Builder.Default
	@Column(name = "is_used", nullable = false)
	private Boolean isUsed = Boolean.TRUE;

	public static UuidFile of(String uuid, String fileKey, String fileUrl, String rawFileName, String extension,
		FilePath filePath) {
		return UuidFile.builder()
			.uuid(uuid)
			.fileKey(fileKey)
			.fileUrl(fileUrl)
			.rawFileName(rawFileName)
			.extension(extension)
			.filePath(filePath)
			.build();
	}

}
