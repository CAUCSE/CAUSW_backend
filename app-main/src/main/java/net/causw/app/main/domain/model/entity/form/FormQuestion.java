package net.causw.app.main.domain.model.entity.form;

import jakarta.persistence.*;
import lombok.*;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.dto.form.request.create.QuestionCreateRequestDto;
import net.causw.app.main.domain.model.enums.form.QuestionType;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_form_question",
	indexes = {
		@Index(name = "form_id_index", columnList = "form_id")
	})
public class FormQuestion extends BaseEntity {

	@Column(name = "number", nullable = false)
	private Integer number;

	@Column(name = "question_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private QuestionType questionType;

	@Column(name = "question_text", nullable = false)
	private String questionText;

	@Column(name = "is_multiple", nullable = false)
	@Builder.Default
	private Boolean isMultiple = false;

	@OneToMany(mappedBy = "formQuestion", cascade = {CascadeType.REMOVE, CascadeType.PERSIST}, orphanRemoval = true)
	@Builder.Default
	private List<FormQuestionOption> formQuestionOptionList = new ArrayList<>();

	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "form_id", nullable = false)
	private Form form;

	public static FormQuestion createObjectiveQuestion(
		Integer number,
		QuestionCreateRequestDto questionCreateRequestDto,
		List<FormQuestionOption> formQuestionOptionList
	) {
		return FormQuestion.builder()
			.number(number)
			.questionType(questionCreateRequestDto.getQuestionType())
			.questionText(questionCreateRequestDto.getQuestionText())
			.isMultiple(questionCreateRequestDto.getIsMultiple())
			.formQuestionOptionList(formQuestionOptionList)
			.build();
	}

	public static FormQuestion createSubjectQuestion(
		Integer number,
		QuestionCreateRequestDto questionCreateRequestDto
	) {
		return FormQuestion.builder()
			.number(number)
			.questionType(questionCreateRequestDto.getQuestionType())
			.questionText(questionCreateRequestDto.getQuestionText())
			.build();
	}

}
