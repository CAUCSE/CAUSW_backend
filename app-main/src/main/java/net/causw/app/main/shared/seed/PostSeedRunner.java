package net.causw.app.main.shared.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("seed")
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class PostSeedRunner implements CommandLineRunner {

	private final BoardSeeder boardSeeder;
	private final PostSeeder postSeeder;
	private final CommentSeeder commentSeeder;
	private final ChildCommentSeeder childCommentSeeder;
	private final InteractionSeeder interactionSeeder;

	@Override
	public void run(String... args) {
		log.info("🌱 Seeding data initialized...");
		boardSeeder.seed();
		postSeeder.seed();
		commentSeeder.seed();
		childCommentSeeder.seed();
		interactionSeeder.seed();
		log.info("🌳 Seeding data finished.");
	}
}
