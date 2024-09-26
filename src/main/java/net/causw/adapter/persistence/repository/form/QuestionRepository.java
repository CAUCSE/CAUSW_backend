package net.causw.adapter.persistence.repository.form;

import net.causw.adapter.persistence.form.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, String> {
    Optional<Question> findById(String id);
}
