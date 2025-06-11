package net.causw.schema;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
public class EntitySchemaValidationTest {

  @Test
  void contextLoads() {
    // ddl-auto=validate 설정 상태에서 Jpa 엔티티와 DB 스키마 검증
  }
}

