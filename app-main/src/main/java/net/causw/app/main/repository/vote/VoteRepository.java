package net.causw.app.main.repository.vote;

import net.causw.app.main.domain.model.entity.vote.Vote;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, String> {
}
