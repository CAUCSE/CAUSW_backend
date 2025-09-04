package net.causw.app.main;

import jakarta.persistence.EntityManagerFactory;

import java.util.Properties;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SchemaValidationTest.Config.class)
public class SchemaValidationTest {

	private static final String targetPackages = "net.causw.app.main.domain.model.entity";

	@Test
	void contextLoads() {
		// 대상 패키지 내의 엔티티와 실제 DB 스키마 간의 유효성 검사 (hibernate.hbm2ddl.auto=validate)
	}

	@TestConfiguration
	@EntityScan(basePackages = targetPackages)
	static class Config {

		@Bean
		public DataSource dataSource(Environment env) {
			return DataSourceBuilder.create()
				.url(env.getProperty("DB_URL"))
				.username(env.getProperty("DB_USERNAME"))
				.password(env.getProperty("DB_PASSWORD"))
				.driverClassName("com.mysql.cj.jdbc.Driver")
				.build();
		}

		@Bean
		public LocalContainerEntityManagerFactoryBean entityManagerFactory(
			DataSource dataSource, JpaVendorAdapter jpaVendorAdapter) {

			LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
			emf.setDataSource(dataSource);
			emf.setPackagesToScan(targetPackages);
			emf.setJpaVendorAdapter(jpaVendorAdapter);
			emf.setJpaProperties(jpaProperties());

			return emf;
		}

		@Bean
		public JpaVendorAdapter jpaVendorAdapter() {
			HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
			adapter.setShowSql(true);
			adapter.setDatabasePlatform("org.hibernate.dialect.MySQL8Dialect");
			return adapter;
		}

		private Properties jpaProperties() {
			Properties properties = new Properties();
			properties.setProperty("hibernate.hbm2ddl.auto", "validate");
			properties.setProperty(
				"hibernate.physical_naming_strategy",
				"org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
			return properties;
		}

		@Bean
		public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
			return new JpaTransactionManager(emf);
		}
	}
}