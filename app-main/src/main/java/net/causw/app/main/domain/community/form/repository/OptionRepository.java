package net.causw.app.main.domain.community.form.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.community.form.entity.FormQuestionOption;

public interface OptionRepository extends JpaRepository<FormQuestionOption, String> {
	Optional<FormQuestionOption> findById(String id);

}