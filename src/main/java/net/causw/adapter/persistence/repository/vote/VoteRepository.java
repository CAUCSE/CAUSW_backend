package net.causw.adapter.persistence.repository.vote;

import net.causw.adapter.persistence.vote.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote,String> {
}
