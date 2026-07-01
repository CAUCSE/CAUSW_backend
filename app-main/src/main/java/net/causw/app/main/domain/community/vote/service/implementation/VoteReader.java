package net.causw.app.main.domain.community.vote.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.vote.entity.Vote;
import net.causw.app.main.domain.community.vote.repository.VoteRepository;
import net.causw.app.main.shared.exception.errorcode.VoteErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteReader {
	private final VoteRepository voteRepository;

	public Vote findById(String voteId) {
		return voteRepository.findById(voteId)
			.orElseThrow(VoteErrorCode.VOTE_NOT_FOUND::toBaseException);
	}

	public Vote findByPostId(String postId) {
		return voteRepository.findByPostId(postId)
			.orElseThrow(VoteErrorCode.VOTE_NOT_FOUND::toBaseException);
	}
}
