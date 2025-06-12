package net.causw.schema;

import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.tool.schema.internal.ExceptionHandlerLoggedImpl;
import org.hibernate.tool.schema.spi.ContributableMatcher;
import org.hibernate.tool.schema.spi.ExceptionHandler;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.tool.schema.spi.SchemaManagementTool;
import org.hibernate.tool.schema.spi.SchemaValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;


@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EntitySchemaValidationTest {

  @Autowired
  private EntityManagerFactory entityManagerFactory;

  @Test
  void validateSchema() {
    SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
    StandardServiceRegistry serviceRegistry = (StandardServiceRegistry) sessionFactory.getServiceRegistry();

    Metadata metadata = new MetadataSources(serviceRegistry).buildMetadata();

    SchemaManagementTool tool = serviceRegistry.getService(SchemaManagementTool.class);

    Map<String, Object> configValues = new HashMap<>();
    SchemaValidator schemaValidator = tool.getSchemaValidator(configValues);

    ExecutionOptions options = new ExecutionOptions() {
      @Override
      public boolean shouldManageNamespaces() {
        return true;
      }

      @Override
      public Map<String, Object> getConfigurationValues() {
        return configValues;
      }

      @Override
      public ExceptionHandler getExceptionHandler() {
        return ExceptionHandlerLoggedImpl.INSTANCE;
      }

      @Override
      public SchemaFilter getSchemaFilter() {
        return SchemaFilter.ALL;
      }
    };

    schemaValidator.doValidation(metadata, options, ContributableMatcher.ALL);
  }
}
