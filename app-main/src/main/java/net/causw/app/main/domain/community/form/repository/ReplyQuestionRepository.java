package net.causw.app.main.domain.community.form.repository;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.community.form.entity.Form;
import net.causw.app.main.domain.community.form.entity.ReplyQuestion;

public interface ReplyQuestionRepository extends JpaRepository<ReplyQuestion, String> {

	@NotNull
	Optional<ReplyQuestion> findById(@NotNull
	String id);

	List<ReplyQuestion> findAllByReplyForm(Form form);

}