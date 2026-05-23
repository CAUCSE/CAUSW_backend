package net.causw.app.main.domain.integration.crawled.entity;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 크롤링된 게시글의 이미지 URL을 저장하는 엔티티.
 * <p>
 * 크롤링 HTML 본문에서 추출한 외부 이미지 URL을 저장합니다.
 * S3에 업로드하지 않으므로 UuidFile 대신 별도 엔티티로 관리합니다.
 */
@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_crawled_post_image")
public class CrawledPostImage extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	@Lob
	@Column(name = "image_url", columnDefinition = "TEXT", nullable = false)
	private String imageUrl;

	@Column(name = "image_order", nullable = false)
	@Builder.Default
	private Integer imageOrder = 0;

	public static CrawledPostImage of(Post post, String imageUrl, int imageOrder) {
		return CrawledPostImage.builder()
			.post(post)
			.imageUrl(imageUrl)
			.imageOrder(imageOrder)
			.build();
	}
}
