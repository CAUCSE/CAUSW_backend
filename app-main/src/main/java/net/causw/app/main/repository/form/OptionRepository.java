package net.causw.app.main.repository.form;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.model.entity.form.FormQuestionOption;

public interface OptionRepository extends JpaRepository<FormQuestionOption, String> {
	Optional<FormQuestionOption> findById(String id);

}