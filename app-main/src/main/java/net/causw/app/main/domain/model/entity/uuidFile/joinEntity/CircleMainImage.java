package net.causw.app.main.domain.model.entity.uuidFile.joinEntity;

import net.causw.app.main.domain.model.entity.circle.Circle;
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
@Table(name = "tb_circle_main_image_uuid_file",
	indexes = {
		@Index(name = "idx_circle_main_image_circle_id", columnList = "circle_id"),
		@Index(name = "idx_circle_main_image_uuid_file_id", columnList = "uuid_file_id")
	})
public class CircleMainImage extends JoinEntity {

	@Getter
	@Setter(AccessLevel.PUBLIC)
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "uuid_file_id", nullable = false, unique = true)
	public UuidFile uuidFile;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "circle_id", nullable = false)
	private Circle circle;

	public static CircleMainImage of(Circle circle, UuidFile uuidFile) {
		return CircleMainImage.builder()
			.circle(circle)
			.uuidFile(uuidFile)
			.build();
	}

}
