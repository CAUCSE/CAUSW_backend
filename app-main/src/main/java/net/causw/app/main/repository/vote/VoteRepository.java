package net.causw.app.main.repository.vote;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.model.entity.vote.Vote;

public interface VoteRepository extends JpaRepository<Vote, String> {
}
