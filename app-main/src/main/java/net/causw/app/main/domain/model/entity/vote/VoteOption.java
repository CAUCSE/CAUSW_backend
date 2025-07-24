package net.causw.app.main.domain.model.entity.vote;

import net.causw.app.main.domain.model.entity.base.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "tb_vote_option")
public class VoteOption extends BaseEntity {

	private String optionName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vote_id", nullable = false)
	private Vote vote;

	public static VoteOption of(String optionName) {
		return VoteOption.builder().optionName(optionName)
			.build();
	}

	public void updateVote(Vote vote) {
		this.vote = vote;
	}
}
