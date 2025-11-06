package net.causw.app.main.domain.moving.repository.vote;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.post.entity.VoteOption;

@Repository
public interface VoteOptionRepository extends JpaRepository<VoteOption, String> {
	List<VoteOption> findByVoteId(String voteId);

}
