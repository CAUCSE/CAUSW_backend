package net.causw.app.main.domain.model.entity.form;

import java.util.List;

import net.causw.app.main.domain.model.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
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
@Table(name = "tb_reply_question",
	indexes = {
		@Index(name = "reply_id_index", columnList = "reply_id"),
		@Index(name = "form_question_id_index", columnList = "form_question_id")
	})
public class ReplyQuestion extends BaseEntity {

	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reply_id", nullable = false)
	private Reply reply;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "form_question_id", nullable = false)
	private FormQuestion formQuestion;

	@Lob // 텍스트 대용량 처리
	@Column(name = "question_answer", columnDefinition = "TEXT")
	private String questionAnswer;

	@Column(name = "selected_option_list", nullable = true)
	private String selectedOptionList;

	public static ReplyQuestion of(
		FormQuestion formQuestion,
		String questionAnswer,
		List<Integer> selectedOptionList
	) {
		return ReplyQuestion.builder()
			.formQuestion(formQuestion)
			.questionAnswer(questionAnswer)
			.selectedOptionList(ReplySelectedOptionManager.fromIntegerList(selectedOptionList).serialize())
			.build();
	}

	public List<Integer> getSelectedOptionList() {
		return ReplySelectedOptionManager.fromString(this.selectedOptionList).getSelectedOptionList();
	}

}
