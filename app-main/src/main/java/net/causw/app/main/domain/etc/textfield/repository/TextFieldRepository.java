package net.causw.app.main.domain.etc.textfield.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.etc.textfield.entity.TextField;

@Repository
public interface TextFieldRepository extends JpaRepository<TextField, String> {
	Optional<TextField> findByKey(String key);
}
