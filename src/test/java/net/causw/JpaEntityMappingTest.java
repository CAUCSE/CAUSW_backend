package net.causw;

import jakarta.persistence.EntityManagerFactory;
import java.util.Properties;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = JpaEntityMappingTest.Config.class)
public class JpaEntityMappingTest {

  private static final String targetPackages = "net.causw.adapter.persistence";

  @Test
  void contextLoads() {
    // Hibernate ddl-auto=validate 실행
  }

  @TestConfiguration
  @EntityScan(basePackages = targetPackages)
  static class Config {

    @Bean
    public DataSource dataSource() {
      return DataSourceBuilder.create()
          .url("jdbc:h2:mem:testdb;MODE=MySQL")
          .username("sa")
          .password("")
          .driverClassName("org.h2.Driver")
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
      adapter.setDatabasePlatform("org.hibernate.dialect.H2Dialect");
      return adapter;
    }

    private Properties jpaProperties() {
      Properties properties = new Properties();
      properties.setProperty("hibernate.hbm2ddl.auto", "validate");
      return properties;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
      return new JpaTransactionManager(emf);
    }
  }
}