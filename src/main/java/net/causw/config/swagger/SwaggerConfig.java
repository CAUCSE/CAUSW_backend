package net.causw.config.swagger;

import net.causw.domain.model.util.StaticValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.*;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@EnableSwagger2
@Configuration
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("net.causw"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(this.apiInfo())
                .useDefaultResponseMessages(false)
                .securityContexts(Collections.singletonList(securityContext()))
                .securitySchemes(List.of(apiKey()))
                .globalOperationParameters(buildGlobalOperationParameters());
    }

    private List<Parameter> buildGlobalOperationParameters() {
        return Arrays.asList(
                new springfox.documentation.builders.ParameterBuilder()
                        .name("Authorization")
                        .description("JWT token")
                        .modelRef(new springfox.documentation.schema.ModelRef("string"))
                        .parameterType("header")
                        .required(true)
                        .build()
        );
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(StaticValue.SWAGGER_API_NAME)
                .version(StaticValue.SWAGGER_API_VERSION)
                .description(StaticValue.SWAGGER_API_DESCRIPTION)
                .build();
    }

    private ApiKey apiKey() {
        return new ApiKey("Authorization", "Authorization", "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.any())
                .build();
    }

    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return List.of(new SecurityReference("Authorization", authorizationScopes));
    }
}