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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JpaEntityMappingTest.Config.class)
public class JpaEntityMappingTest {

  private static final String targetPackages = "net.causw.adapter.persistence";

  @Test
  void contextLoads() {
    // targetPackages 내의 JPA 엔티티와 DB 스키마 일치 검증
  }

  @TestConfiguration
  @EntityScan(basePackages = targetPackages)
  static class Config {

    @Bean
    public DataSource dataSource() {
      return DataSourceBuilder.create()
          .url("jdbc:mysql://localhost:3306/causw_test") // 테스트용 MySQL Docker 컨테이너 설정
          .username("root")
          .password("password")
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
          "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy"); // 엔티티의 camel case를 snake case로, 대문자를 소문자로 변경
      return properties;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
      return new JpaTransactionManager(emf);
    }
  }
}