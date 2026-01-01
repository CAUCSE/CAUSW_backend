package net.causw.app.main.domain.community.vote.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.community.vote.entity.Vote;

public interface VoteRepository extends JpaRepository<Vote, String> {}
