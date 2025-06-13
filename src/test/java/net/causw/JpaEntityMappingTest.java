package net.causw;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@ComponentScan(basePackages = "net.causw.adapter.persistence")
public class JpaEntityMappingTest {

  @Test
  void contextLoads() {
    // Hibernate ddl-auto=validate 실행
  }
}