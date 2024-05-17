package net.causw.application.spi;

import java.util.Optional;

public interface FlagPort {
    Optional<Boolean> findByKey(String key);

    Boolean create(String key, Boolean value);

    Optional<Boolean> update(String key, Boolean value);
}
