package net.causw.adapter.persistence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = "net.causw.adapter.persistence")
@EntityScan(basePackages = "net.causw.adapter.persistence")
public class EntitySchemaValidationTest {
  public static void main(String[] args) {
    ConfigurableApplicationContext ctx = SpringApplication.run(EntitySchemaValidationTest.class, args);
    ctx.close();
  }
}