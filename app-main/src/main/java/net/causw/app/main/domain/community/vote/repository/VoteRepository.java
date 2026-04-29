package net.causw.app.main.domain.community.vote.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.community.vote.entity.Vote;

public interface VoteRepository extends JpaRepository<Vote, String> {
    Optional<Vote> findByPostId(String postId);
}
