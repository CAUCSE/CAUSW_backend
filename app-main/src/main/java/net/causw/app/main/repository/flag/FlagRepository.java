package net.causw.app.main.repository.flag;

import net.causw.app.main.domain.model.entity.flag.Flag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FlagRepository extends JpaRepository<Flag, String> {
    Optional<Flag> findByKey(String key);

    Optional<Flag> findByValue(Boolean value);
}
