package net.causw.adapter.persistence.repository.form;

import net.causw.adapter.persistence.form.FormQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<FormQuestion, String> {
    Optional<FormQuestion> findById(String id);
}
