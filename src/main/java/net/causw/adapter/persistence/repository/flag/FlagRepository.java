package net.causw.adapter.persistence.repository.flag;

import net.causw.adapter.persistence.flag.Flag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FlagRepository extends JpaRepository<Flag, String> {
    Optional<Flag> findByKey(String key);

    Optional<Flag> findByValue(Boolean value);
}
