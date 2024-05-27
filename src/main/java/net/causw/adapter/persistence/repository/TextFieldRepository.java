package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.textfield.TextField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TextFieldRepository extends JpaRepository<TextField, String> {
    Optional<TextField> findByKey(String key);
}
