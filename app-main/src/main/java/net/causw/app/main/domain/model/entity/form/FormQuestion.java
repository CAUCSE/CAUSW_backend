package net.causw.app.main.domain.model.entity.form;

import java.util.ArrayList;
import java.util.List;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.enums.form.QuestionType;
import net.causw.app.main.dto.form.request.create.QuestionCreateRequestDto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
