package net.causw.app.main.domain.community.vote.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.vote.entity.Vote;
import net.causw.app.main.domain.community.vote.repository.VoteRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class VoteWriter {
	private final VoteRepository voteRepository;

	public Vote save(Vote vote) {
		return voteRepository.save(vote);
	}

	public void delete(Vote vote) {
		voteRepository.delete(vote);
	}
}
