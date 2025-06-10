package net.causw.config.flyway;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class FlywayConfig {

  /**
   * 자동으로 마이그레이션 실패 기록을 정리하고 해당 마이그레이션을 재시도합니다.
   * 자동 기록 정리는 위험할 수 있으나, 개발 편의 목적으로 local 프로필에서만 활성화합니다.
   */
  @Bean
  @Profile("local")
  public FlywayMigrationStrategy repairAndMigrateStrategy() {
    return flyway -> {
      flyway.repair();
      flyway.migrate();
    };
  }
}
