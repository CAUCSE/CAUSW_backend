package net.causw.app.main.domain.moving.repository.flag;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.moving.model.entity.flag.Flag;

public interface FlagRepository extends JpaRepository<Flag, String> {
	Optional<Flag> findByKey(String key);

	Optional<Flag> findByValue(Boolean value);
}
