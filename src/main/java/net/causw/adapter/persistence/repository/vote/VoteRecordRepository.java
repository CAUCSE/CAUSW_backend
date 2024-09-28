package net.causw.adapter.persistence.repository.vote;

import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.vote.Vote;
import net.causw.adapter.persistence.vote.VoteOption;
import net.causw.adapter.persistence.vote.VoteRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteRecordRepository extends JpaRepository<VoteRecord ,String> {
    List<VoteRecord> findAllByVoteOption(VoteOption voteOption);
    List<VoteRecord> findByVoteOption_VoteAndUser(Vote vote, User user);
    boolean existsByVoteOption_VoteAndUser(Vote vote, User user);
}
