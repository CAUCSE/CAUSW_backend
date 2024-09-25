package net.causw.adapter.persistence.repository.form;

import net.causw.adapter.persistence.form.Form;
import net.causw.adapter.persistence.form.ReplyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReplyQuestionRepository extends JpaRepository<ReplyQuestion, String> {

    Optional<ReplyQuestion> findById(String id);

    List<ReplyQuestion> findAllByForm(Form form);

}