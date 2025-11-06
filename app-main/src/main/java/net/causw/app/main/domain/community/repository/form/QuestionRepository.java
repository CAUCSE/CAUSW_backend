package net.causw.app.main.domain.community.repository.form;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.community.entity.form.FormQuestion;

public interface QuestionRepository extends JpaRepository<FormQuestion, String> {
	Optional<FormQuestion> findById(String id);
}
