package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.form.Option;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OptionRepository  extends JpaRepository<Option, String> {
    Optional<Option> findById(String id);

}