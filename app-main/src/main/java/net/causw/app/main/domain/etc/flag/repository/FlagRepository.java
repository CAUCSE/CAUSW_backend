package net.causw.app.main.domain.etc.flag.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.etc.flag.entity.Flag;

public interface FlagRepository extends JpaRepository<Flag, String> {
	Optional<Flag> findByKey(String key);

	Optional<Flag> findByValue(Boolean value);
}
