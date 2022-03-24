package net.causw.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlagRepository extends JpaRepository<Flag, String> {
    Optional<Flag> findByKey(String key);
}
