package net.causw.adapter.persistence.repository.vote;

import net.causw.adapter.persistence.vote.VoteOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteOptionRepository extends JpaRepository<VoteOption, String> {
    List<VoteOption> findByVoteId(String voteId);

}
