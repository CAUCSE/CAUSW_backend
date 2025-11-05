package net.causw.app.main.domain.moving.repository.form;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.moving.model.entity.form.FormQuestionOption;

public interface OptionRepository extends JpaRepository<FormQuestionOption, String> {
	Optional<FormQuestionOption> findById(String id);

}