package net.causw.app.main.core.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import net.causw.app.main.core.filter.SoftDeleteFilterInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	private final OctetStreamReadMsgConverter octetStreamReadMsgConverter;
	private final SoftDeleteFilterInterceptor softDeleteFilterInterceptor;

	@Autowired
	public WebConfig(
		OctetStreamReadMsgConverter octetStreamReadMsgConverter,
		SoftDeleteFilterInterceptor softDeleteFilterInterceptor) {
		this.octetStreamReadMsgConverter = octetStreamReadMsgConverter;
		this.softDeleteFilterInterceptor = softDeleteFilterInterceptor;
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(octetStreamReadMsgConverter);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(softDeleteFilterInterceptor);
	}
}
