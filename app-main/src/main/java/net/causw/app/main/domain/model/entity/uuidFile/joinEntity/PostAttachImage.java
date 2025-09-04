package net.causw.app.main.domain.model.entity.uuidFile.joinEntity;

import net.causw.app.main.domain.model.entity.post.Post;
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
@Table(name = "tb_post_attach_image_uuid_file",
	indexes = {
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

	public static PostAttachImage of(Post post, UuidFile uuidFile) {
		return PostAttachImage.builder()
			.uuidFile(uuidFile)
			.post(post)
			.build();
	}

}
