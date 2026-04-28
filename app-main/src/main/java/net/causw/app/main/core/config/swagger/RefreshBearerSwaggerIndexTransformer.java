package net.causw.app.main.core.config.swagger;

import static org.springdoc.core.utils.Constants.SWAGGER_INITIALIZER_JS;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.core.io.Resource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import jakarta.servlet.http.HttpServletRequest;

public class RefreshBearerSwaggerIndexTransformer extends SwaggerIndexPageTransformer {

	private static final String SWAGGER_BUNDLE_START = "SwaggerUIBundle({";

	private static final String REQUEST_INTERCEPTOR = """
			requestInterceptor: function(request) {
			  var headers = request.headers || {};
			  var authorization = headers['Authorization'];
			  if (typeof authorization === 'string') {
			    var authTrimmed = authorization.trim();
			    if (authTrimmed !== '' && !/^Bearer\\s+/i.test(authTrimmed)) {
			      headers['Authorization'] = 'Bearer ' + authTrimmed;
			    }
			  }
			  var refreshAuthorization = headers['Refresh-Authorization'];
			  if (typeof refreshAuthorization === 'string') {
			    var refreshTrimmed = refreshAuthorization.trim();
			    if (refreshTrimmed !== '' && !/^Bearer\\s+/i.test(refreshTrimmed)) {
			      headers['Refresh-Authorization'] = 'Bearer ' + refreshTrimmed;
			    }
			  }
			  request.headers = headers;
			  return request;
			},
		""";

	public RefreshBearerSwaggerIndexTransformer(SwaggerUiConfigProperties swaggerUiConfig,
		SwaggerUiOAuthProperties swaggerUiOAuthProperties,
		SwaggerUiConfigParameters swaggerUiConfigParameters,
		SwaggerWelcomeCommon swaggerWelcomeCommon,
		ObjectMapperProvider objectMapperProvider) {
		super(swaggerUiConfig, swaggerUiOAuthProperties, swaggerUiConfigParameters,
			swaggerWelcomeCommon, objectMapperProvider);
	}

	@Override
	public Resource transform(HttpServletRequest request, Resource resource,
		ResourceTransformerChain transformerChain) throws IOException {
		Resource transformed = super.transform(request, resource, transformerChain);

		AntPathMatcher antPathMatcher = new AntPathMatcher();
		boolean isInitializer = antPathMatcher.match("**/swagger-ui/**/" + SWAGGER_INITIALIZER_JS,
			resource.getURL().toString());

		if (!isInitializer) {
			return transformed;
		}

		String initializerScript = new String(transformed.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		return new TransformedResource(transformed,
			injectRequestInterceptor(initializerScript).getBytes(StandardCharsets.UTF_8));
	}

	private String injectRequestInterceptor(String initializerScript) {
		if (initializerScript.contains("requestInterceptor:")) {
			return initializerScript;
		}

		int bundleStart = initializerScript.indexOf(SWAGGER_BUNDLE_START);
		if (bundleStart < 0) {
			return initializerScript;
		}

		int insertPosition = bundleStart + SWAGGER_BUNDLE_START.length();
		return initializerScript.substring(0, insertPosition)
			+ System.lineSeparator()
			+ REQUEST_INTERCEPTOR
			+ initializerScript.substring(insertPosition);
	}
}
