package net.causw.app.main.core.config.swagger;

import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
	/**
	 * 전체 API 그룹
	 * - 경로: /api/** (v1, v2 등 버전 상관 없이 모두 포함)
	 */
	@Bean
	public GroupedOpenApi allApi() {
		return GroupedOpenApi.builder()
			.group("all")
			.pathsToMatch("/api/**")
			.build();
	}

	/**
	 * v2 API 그룹
	 * - 패키지: net.causw.app.main.domain..api.v2.controller (추가 예정)
	 * - 경로:   /api/v2/**
	 *
	 * 현재는 v2 컨트롤러가 없어도 그룹만 먼저 생성해 두고,
	 * 이후 v2 패키지를 만들면 자동으로 분리되어 보이게 됩니다.
	 */
	@Bean
	public GroupedOpenApi v2Api() {
		return GroupedOpenApi.builder()
			.group("v2")
			.pathsToMatch("/api/v2/**")
			.build();
	}

	/**
	 * v2 에서는 관리자 api에 prefix를 붙일 예정입니다
	 * - 패키지: net.causw.app.main.domain..api.v2.admin.controller (추가 예정)
	 * - 경로:   /api/v2/**
	 */
	@Bean
	public GroupedOpenApi v2AdminApi() {
		return GroupedOpenApi.builder()
			.group("admin-v2")
			.pathsToMatch("/api/v2/admin/**")
			.build();
	}

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
			.components(new Components()
				.addSecuritySchemes("bearerAuth",
					new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
						.in(SecurityScheme.In.HEADER).name("Authorization"))
				.addSecuritySchemes("refreshBearerAuth",
					new SecurityScheme().type(SecurityScheme.Type.APIKEY)
						.in(SecurityScheme.In.HEADER).name("Refresh-Authorization")))
			.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
			.info(new Info().title(StaticValue.SWAGGER_API_NAME)
				.version(StaticValue.SWAGGER_API_VERSION)
				.description(StaticValue.SWAGGER_API_DESCRIPTION));
	}

	@Bean
	@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true", matchIfMissing = true)
	public SwaggerIndexTransformer refreshBearerSwaggerIndexTransformer(
		SwaggerUiConfigProperties swaggerUiConfig,
		SwaggerUiOAuthProperties swaggerUiOAuthProperties,
		SwaggerWelcomeCommon swaggerWelcomeCommon,
		ObjectMapperProvider objectMapperProvider) {
		return new RefreshBearerSwaggerIndexTransformer(swaggerUiConfig, swaggerUiOAuthProperties,
			swaggerWelcomeCommon, objectMapperProvider);
	}
}
