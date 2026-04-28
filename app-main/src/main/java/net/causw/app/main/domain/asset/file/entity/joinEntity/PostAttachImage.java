package net.causw.app.main.domain.asset.file.entity.joinEntity;

import org.hibernate.annotations.ColumnDefault;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.community.post.entity.Post;

import jakarta.persistence.Column;
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
@Table(name = "tb_post_attach_image_uuid_file", indexes = {
	@Index(name = "idx_post_attach_image_post_id", columnList = "post_id"),
	@Index(name = "idx_post_attach_image_uuid_file_id", columnList = "uuid_file_id")
})
public class PostAttachImage extends JoinEntity {

	@Getter
	@Setter(AccessLevel.PUBLIC)
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "uuid_file_id", nullable = false, unique = true)
	public UuidFile uuidFile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	@Setter
	@Column(name = "image_order", nullable = false)
	@ColumnDefault("0")
	@Builder.Default
	private Integer imageOrder = 0;

	@Setter
	@Column(name = "is_representative", nullable = false)
	@ColumnDefault("false")
	@Builder.Default
	private Boolean isRepresentative = false;

	public static PostAttachImage of(Post post, UuidFile uuidFile) {
		return PostAttachImage.builder()
			.uuidFile(uuidFile)
			.post(post)
			.build();
	}

	public static PostAttachImage of(Post post, UuidFile uuidFile, Integer imageOrder, Boolean isRepresentative) {
		return PostAttachImage.builder()
			.uuidFile(uuidFile)
			.post(post)
			.imageOrder(imageOrder)
			.isRepresentative(isRepresentative)
			.build();
	}

}
