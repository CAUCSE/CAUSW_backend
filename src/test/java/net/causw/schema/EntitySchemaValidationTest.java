package net.causw.schema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;

@EntityScan(basePackages = {"co.dalicious.*"})
@SpringBootApplication
public class EntitySchemaValidationTest {
  public static void main(String[] args) {
    ConfigurableApplicationContext ctx = SpringApplication.run(EntitySchemaValidationTest.class, args);
    ctx.close();
  }
}