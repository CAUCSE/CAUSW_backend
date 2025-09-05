package net.causw.app.main.repository.flag;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.model.entity.flag.Flag;

public interface FlagRepository extends JpaRepository<Flag, String> {
	Optional<Flag> findByKey(String key);

	Optional<Flag> findByValue(Boolean value);
}
