package net.causw.application.spi;

import java.util.Optional;

public interface TextFieldPort {
    Optional<String> findByKey(String key);

    String create(String key, String value);

    Optional<String> update(String key, String value);
}
