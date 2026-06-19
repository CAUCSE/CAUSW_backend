package net.causw.app.main.domain.community.form.repository;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.form.entity.Form;

@Repository
public interface FormRepository extends JpaRepository<Form, String> {
	@NotNull
	Optional<Form> findByIdAndIsDeleted(@NotNull String id, Boolean isDeleted);
}
