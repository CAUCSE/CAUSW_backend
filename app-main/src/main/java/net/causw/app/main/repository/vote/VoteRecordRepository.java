package net.causw.app.main.repository.vote;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.vote.Vote;
import net.causw.app.main.domain.model.entity.vote.VoteOption;
import net.causw.app.main.domain.model.entity.vote.VoteRecord;

public interface VoteRecordRepository extends JpaRepository<VoteRecord, String> {
	List<VoteRecord> findAllByVoteOption(VoteOption voteOption);

	List<VoteRecord> findByVoteOption_VoteAndUser(Vote vote, User user);

	boolean existsByVoteOption_VoteAndUser(Vote vote, User user);
}
