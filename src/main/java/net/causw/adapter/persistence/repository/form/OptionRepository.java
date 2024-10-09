package net.causw.adapter.persistence.repository.form;

import net.causw.adapter.persistence.form.FormQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OptionRepository  extends JpaRepository<FormQuestionOption, String> {
    Optional<FormQuestionOption> findById(String id);

}