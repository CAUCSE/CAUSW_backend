package net.causw.app.main.domain.model.entity.form;

import java.util.ArrayList;
import java.util.List;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_reply")
public class Reply extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "form_id", nullable = false)
	private Form form;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@OneToMany(mappedBy = "reply", cascade = {CascadeType.REMOVE, CascadeType.PERSIST}, orphanRemoval = true)
	@Builder.Default
	private List<ReplyQuestion> replyQuestionList = new ArrayList<>();

	public static Reply of(
		Form form,
		User user,
		List<ReplyQuestion> replyQuestionList
	) {
		return Reply.builder()
			.form(form)
			.user(user)
			.replyQuestionList(replyQuestionList)
			.build();
	}
}
