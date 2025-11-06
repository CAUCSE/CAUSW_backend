package net.causw.app.main.domain.moving.repository.vote;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.community.post.entity.Vote;
import net.causw.app.main.domain.community.post.entity.VoteOption;
import net.causw.app.main.domain.community.post.entity.VoteRecord;
import net.causw.app.main.domain.user.account.entity.user.User;

public interface VoteRecordRepository extends JpaRepository<VoteRecord, String> {
	List<VoteRecord> findAllByVoteOption(VoteOption voteOption);

	List<VoteRecord> findByVoteOption_VoteAndUser(Vote vote, User user);

	boolean existsByVoteOption_VoteAndUser(Vote vote, User user);
}
