package net.causw.app.main.domain.model.entity.uuidFile.joinEntity;

import net.causw.app.main.domain.model.entity.ceremony.Ceremony;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "tb_ceremony_attach_image_uuid_file",
	indexes = {
		@Index(name = "idx_ceremony_attach_image_ceremony_id", columnList = "ceremony_id"),
		@Index(name = "idx_ceremony_attach_image_uuid_file_id", columnList = "uuid_file_id")
	})
public class CeremonyAttachImage extends JoinEntity {
	@Getter
	@Setter(AccessLevel.PUBLIC)
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "uuid_file_id", nullable = false, unique = true)
	public UuidFile uuidFile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ceremony_id", nullable = false)
	private Ceremony ceremony;

	public static CeremonyAttachImage of(Ceremony ceremony, UuidFile uuidFile) {
		return CeremonyAttachImage.builder()
			.uuidFile(uuidFile)
			.ceremony(ceremony)
			.build();
	}
}

