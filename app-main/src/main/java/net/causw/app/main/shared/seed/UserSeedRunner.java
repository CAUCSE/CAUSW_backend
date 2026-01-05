package net.causw.app.main.shared.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("seed")
public class UserSeedRunner implements CommandLineRunner {

	private final UserSeeder userSeeder;

	public UserSeedRunner(UserSeeder userSeeder) {
		this.userSeeder = userSeeder;
	}

	@Override
	public void run(String... args) {
		int count = args.length > 0 ? Integer.parseInt(args[0]) : 10_000;
		userSeeder.seed(count);
	}
}
