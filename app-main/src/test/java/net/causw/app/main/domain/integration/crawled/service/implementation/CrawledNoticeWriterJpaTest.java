package net.causw.app.main.domain.integration.crawled.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import net.causw.app.main.domain.integration.crawled.dto.CleanArticle;
import net.causw.app.main.domain.integration.crawled.dto.CleanAttachment;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.repository.CrawledNoticeRepository;

import jakarta.persistence.EntityManagerFactory;

/**
 * Repository를 Mock으로 대체하지 않고 실제 JPA(H2)로 갱신 경로를 검증
 * 테스트 자체는 트랜잭션 밖에서 실행해, 조회한 엔티티가 준영속 상태로 갱신에 전달되는 운영 흐름을 재현
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CrawledNoticeWriterJpaTest.JpaTestConfig.class)
class CrawledNoticeWriterJpaTest {

	private static final String SITE_ID = "cau-sw-notice";
	private static final String EXTERNAL_ID = "oktomato_bbs05:3451";

	@Autowired
	private CrawledNoticeWriter crawledNoticeWriter;

	@Autowired
	private CrawledNoticeRepository crawledNoticeRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("첨부가 있는 공지를 준영속 상태에서 갱신하면 기존 첨부는 지워지고 새 첨부가 값과 함께 저장된다")
	void update_detachedNoticeWithAttachments_replacesAttachments() {
		crawledNoticeWriter.save(article("원본 제목", "hash-1",
			List.of(new CleanAttachment("old.pdf", "https://example.com/old.pdf"))));

		// 트랜잭션 밖에서 조회하므로 existing은 준영속 상태다.
		CrawledNotice existing = crawledNoticeRepository
			.findBySiteIdAndExternalIdIn(SITE_ID, List.of(EXTERNAL_ID)).get(0);

		crawledNoticeWriter.update(existing, article("수정 제목", "hash-2",
			List.of(new CleanAttachment("new.pdf", "https://example.com/new.pdf"))));

		CrawledNotice reloaded = crawledNoticeRepository
			.findBySiteIdAndExternalIdIn(SITE_ID, List.of(EXTERNAL_ID)).get(0);
		assertThat(reloaded.getTitle()).isEqualTo("수정 제목");
		assertThat(reloaded.getContentHash()).isEqualTo("hash-2");
		assertThat(reloaded.getCrawledFileLinks())
			.extracting("fileName", "fileLink")
			.containsExactly(org.assertj.core.groups.Tuple.tuple("new.pdf", "https://example.com/new.pdf"));
		// 고아가 된 기존 첨부 row가 남아있지 않아야 한다.
		assertThat(jdbcTemplate.queryForObject("select count(*) from tb_crawled_file_link", Integer.class))
			.isEqualTo(1);
	}

	private CleanArticle article(String title, String hash, List<CleanAttachment> attachments) {
		return CleanArticle.of(SITE_ID, "target-board-id", EXTERNAL_ID, "https://example.com/3451", "공지사항",
			title, "내용", "관리자", LocalDate.now(), null, attachments, hash);
	}

	@Configuration
	@EnableTransactionManagement
	@EnableJpaRepositories(basePackageClasses = CrawledNoticeRepository.class)
	static class JpaTestConfig {

		@Bean
		CrawledNoticeWriter crawledNoticeWriter(CrawledNoticeRepository crawledNoticeRepository) {
			return new CrawledNoticeWriter(crawledNoticeRepository);
		}

		@Bean
		JdbcTemplate jdbcTemplate(DataSource dataSource) {
			return new JdbcTemplate(dataSource);
		}

		@Bean
		DataSource dataSource() {
			return new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.H2)
				.generateUniqueName(true)
				.build();
		}

		@Bean
		LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
			LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
			factory.setDataSource(dataSource);
			factory.setPackagesToScan("net.causw.app.main.domain");
			factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
			Properties properties = new Properties();
			properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
			factory.setJpaProperties(properties);
			return factory;
		}

		@Bean
		PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
			return new JpaTransactionManager(entityManagerFactory);
		}
	}
}
