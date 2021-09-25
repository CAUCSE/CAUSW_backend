package net.causw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CauswApplication {
    public static void main(String[] args) {
        SpringApplication.run(CauswApplication.class, args);
    }
}
