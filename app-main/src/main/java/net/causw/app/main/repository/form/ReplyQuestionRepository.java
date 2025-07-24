package net.causw.app.main.repository.form;

import net.causw.app.main.domain.model.entity.form.Form;
import net.causw.app.main.domain.model.entity.form.ReplyQuestion;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReplyQuestionRepository extends JpaRepository<ReplyQuestion, String> {

	@NotNull
	Optional<ReplyQuestion> findById(@NotNull String id);

	List<ReplyQuestion> findAllByReplyForm(Form form);

}