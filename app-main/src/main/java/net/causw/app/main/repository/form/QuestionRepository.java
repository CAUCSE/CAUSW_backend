package net.causw.app.main.repository.form;

import net.causw.app.main.domain.model.entity.form.FormQuestion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<FormQuestion, String> {
	Optional<FormQuestion> findById(String id);
}
