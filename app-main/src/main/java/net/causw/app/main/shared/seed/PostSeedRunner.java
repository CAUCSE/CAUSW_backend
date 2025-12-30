package net.causw.app.main.shared.seed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("seed")
@Slf4j
public class PostSeedRunner implements CommandLineRunner {

    private final BoardSeeder boardSeeder;

    public PostSeedRunner(BoardSeeder boardSeeder) {
        this.boardSeeder = boardSeeder;
    }

    @Override
    public void run(String... args) {
        log.info("🌱 Seeding data initialized...");
        // NOTE: 유저 시딩 필수
        boardSeeder.seed();
        log.info("🌳 Seeding data finished.");
    }
}
