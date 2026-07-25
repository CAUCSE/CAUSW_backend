package net.causw.app.main;

import java.util.Arrays;
import java.util.TimeZone;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@OpenAPIDefinition(servers = {
	@Server(url = "/", description = "Default Server URL")
})
@SpringBootApplication
public class CauswApplication {

	private final Environment environment;

	static {
		System.setProperty("aws.ec2MetadataDisabled", "true");
	}

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(CauswApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);
	}

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

	@EventListener(ApplicationReadyEvent.class)
	@Order(2)
	public void onApplicationReady() {
		String profiles = String.join(", ", Arrays.asList(environment.getActiveProfiles()));
		if (profiles.isBlank()) {
			profiles = "default";
		}

		log.info("\n"
			+ "   ██████╗  █████╗ ██╗   ██╗███████╗██╗    ██╗\n"
			+ "  ██╔════╝ ██╔══██╗██║   ██║██╔════╝██║    ██║\n"
			+ "  ██║      ███████║██║   ██║███████╗██║ █╗ ██║\n"
			+ "  ██║      ██╔══██║██║   ██║╚════██║██║███╗██║\n"
			+ "  ╚██████╗ ██║  ██║╚██████╔╝███████║╚███╔███╔╝\n"
			+ "   ╚═════╝ ╚═╝  ╚═╝ ╚═════╝ ╚══════╝ ╚══╝╚══╝\n"
			+ "  :: CAU Student Web Application ::");
		log.info("Spring Boot    : {}", SpringBootVersion.getVersion());
		log.info("Java Version   : {}", System.getProperty("java.version"));
		log.info("Active Profile : {}", profiles);
	}
}
