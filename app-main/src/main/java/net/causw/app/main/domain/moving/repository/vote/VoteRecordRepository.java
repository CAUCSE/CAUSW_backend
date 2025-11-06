package net.causw.app.main.domain.moving.repository.vote;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.community.entity.vote.Vote;
import net.causw.app.main.domain.community.entity.vote.VoteOption;
import net.causw.app.main.domain.community.entity.vote.VoteRecord;
import net.causw.app.main.domain.user.entity.user.User;

public interface VoteRecordRepository extends JpaRepository<VoteRecord, String> {
	List<VoteRecord> findAllByVoteOption(VoteOption voteOption);

	List<VoteRecord> findByVoteOption_VoteAndUser(Vote vote, User user);

	boolean existsByVoteOption_VoteAndUser(Vote vote, User user);
}
