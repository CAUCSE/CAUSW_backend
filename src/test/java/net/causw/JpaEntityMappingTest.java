package net.causw;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public class JpaEntityMappingTest {

  @Test
  void contextLoads() {
    // Hibernate ddl-auto=validate 실행
  }
}