package net.causw.app.main.domain.model.entity.post;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.form.Form;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.PostAttachImage;
import net.causw.app.main.domain.model.entity.vote.Vote;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "tb_post", indexes = {
	@Index(name = "board_id_index", columnList = "board_id"),
	@Index(name = "user_id_index", columnList = "user_id"),
	@Index(name = "form_id_index", columnList = "form_id")
})
public class Post extends BaseEntity {
	@Column(name = "title", nullable = false)
	private String title;

	@Lob
	@Column(columnDefinition = "TEXT", name = "content", nullable = false)
	private String content;

	@OneToMany(cascade = {CascadeType.REMOVE, CascadeType.PERSIST}, mappedBy = "post")
	@Builder.Default
	private List<PostAttachImage> postAttachImageList = new ArrayList<>();

	@ManyToOne(targetEntity = User.class)
	@JoinColumn(name = "user_id", nullable = false)
	private User writer;

	@Column(name = "is_deleted")
	@Builder.Default
	@ColumnDefault("false")
	private Boolean isDeleted = false;

	@Column(name = "is_anonymous", nullable = false)
	@ColumnDefault("false")
	private Boolean isAnonymous;

	@Column(name = "is_question", nullable = false)
	@ColumnDefault("false")
	private Boolean isQuestion;

	@ManyToOne(targetEntity = Board.class)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	@OneToOne(cascade = {CascadeType.REMOVE, CascadeType.PERSIST})
	@JoinColumn(name = "form_id", unique = true)
	private Form form;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "vote_id", unique = true)
	private Vote vote;

	public static Post of(
		String title,
		String content,
		User writer,
		Boolean isAnonymous,
		Boolean isQuestion,
		Board board,
		Form form,

		List<UuidFile> postAttachImageUuidFileList
	) {
		Post post = Post.builder()
			.title(title)
			.content(content)
			.writer(writer)
			.isAnonymous(isAnonymous)
			.isQuestion(isQuestion)
			.board(board)
			.form(form)
			.build();

		if (postAttachImageUuidFileList.isEmpty()) {
			return post;
		}

		List<PostAttachImage> postAttachImageList = postAttachImageUuidFileList.stream()
			.map(uuidFile -> PostAttachImage.of(post, uuidFile))
			.toList();

		post.setPostAttachFileList(postAttachImageList);

		return post;
	}

	public void update(String title, String content, Form form, List<PostAttachImage> postAttachImageList) {
		this.title = title;
		this.content = content;
		this.form = form;
		this.postAttachImageList = postAttachImageList;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
		if (form != null) {
			this.form.setIsDeleted(isDeleted);
		}
	}

	private void setPostAttachFileList(List<PostAttachImage> postAttachImageList) {
		this.postAttachImageList = postAttachImageList;
	}

	public void updateVote(Vote vote) {
		this.vote = vote;
	}
}
