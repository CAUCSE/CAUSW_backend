package net.causw.app.main.domain.community.form.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.form.entity.Form;

@Repository
public interface FormRepository extends JpaRepository<Form, String> {
	Optional<Form> findByIdAndIsDeleted(String id, Boolean isDeleted);
}
