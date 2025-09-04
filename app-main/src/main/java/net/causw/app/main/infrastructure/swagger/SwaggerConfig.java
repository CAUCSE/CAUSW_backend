package net.causw.app.main.infrastructure.swagger;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.causw.global.constant.StaticValue;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {
	@Bean
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder()
			.group("public")
			.pathsToMatch("/**")
			.build();
	}

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
			.components(new Components()
				.addSecuritySchemes("bearerAuth",
					new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
						.in(SecurityScheme.In.HEADER).name("Authorization")))
			.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
			.info(new Info().title(StaticValue.SWAGGER_API_NAME)
				.version(StaticValue.SWAGGER_API_VERSION)
				.description(StaticValue.SWAGGER_API_DESCRIPTION));
	}
}
