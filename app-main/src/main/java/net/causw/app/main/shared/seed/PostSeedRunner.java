package net.causw.app.main.shared.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("seed")
@RequiredArgsConstructor
@Slf4j
public class PostSeedRunner implements CommandLineRunner {

    private final BoardSeeder boardSeeder;
    private final PostSeeder postSeeder;
    private final CommentSeeder commentSeeder;
    private final ChildCommentSeeder childCommentSeeder;
    private final InteractionSeeder interactionSeeder;

    @Override
    public void run(String... args) {
        log.info("🌱 Seeding data initialized...");
        // NOTE: 유저 시딩 필수
        boardSeeder.seed();
        postSeeder.seed();
        commentSeeder.seed();
        childCommentSeeder.seed();
        interactionSeeder.seed();
        log.info("🌳 Seeding data finished.");
    }
}
