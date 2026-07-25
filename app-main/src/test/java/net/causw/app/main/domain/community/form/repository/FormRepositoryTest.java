package net.causw.app.main.domain.community.form.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManagerFactory;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = FormRepositoryTest.JpaTestConfig.class)
@Transactional
class FormRepositoryTest {

	@Autowired
	private FormRepository formRepository;

	@Test
	@DisplayName("게시판별 연결 폼 소프트 삭제 JPQL을 실행할 수 있다")
	void softDeleteAllByBoardId_shouldExecuteValidUpdateQuery() {
		assertThat(formRepository.softDeleteAllByBoardId("missing-board-id")).isZero();
	}

	@Configuration
	@EnableTransactionManagement
	@EnableJpaRepositories(basePackageClasses = FormRepository.class)
	static class JpaTestConfig {

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
