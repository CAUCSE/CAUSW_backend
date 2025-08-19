package net.causw.app.main.repository.textField;

import net.causw.app.main.domain.model.entity.textfield.TextField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TextFieldRepository extends JpaRepository<TextField, String> {
    Optional<TextField> findByKey(String key);
}
