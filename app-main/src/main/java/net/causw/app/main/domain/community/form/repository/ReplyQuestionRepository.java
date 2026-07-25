package net.causw.app.main.domain.community.form.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import net.causw.app.main.domain.community.form.entity.Form;
import net.causw.app.main.domain.community.form.entity.ReplyQuestion;

public interface ReplyQuestionRepository extends JpaRepository<ReplyQuestion, String> {

	@NonNull
	Optional<ReplyQuestion> findById(@NonNull String id);

	List<ReplyQuestion> findAllByReplyForm(Form form);

}