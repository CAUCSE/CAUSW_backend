package net.causw.app.main.repository.form;

import net.causw.app.main.domain.model.entity.form.FormQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OptionRepository  extends JpaRepository<FormQuestionOption, String> {
    Optional<FormQuestionOption> findById(String id);

}