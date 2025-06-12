package net.causw;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class EntitySchemaValidationTest {

  @Test
  void contextLoads() {
    // Hibernate ddl-auto=validate 설정이 있으면, 여기서 검증됨
  }
}