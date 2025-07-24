package net.causw.app.main.domain.model.entity.crawled;

import net.causw.app.main.domain.model.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_crawled_file_link")
public class CrawledFileLink extends BaseEntity {
	@Column(name = "file_name", nullable = false)
	private String fileName;

	@Column(name = "file_link", nullable = false)
	private String fileLink;

	public static CrawledFileLink of(
		String fileName,
		String fileLink
	) {
		return CrawledFileLink.builder()
			.fileName(fileName)
			.fileLink(fileLink)
			.build();
	}
}
