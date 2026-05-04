package net.causw.app.main.domain.community.vote.service.v2.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.vote.entity.VoteOption;
import net.causw.app.main.domain.community.vote.repository.VoteOptionRepository;
import net.causw.app.main.shared.exception.errorcode.VoteErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteOptionReader {
	private final VoteOptionRepository voteOptionRepository;

	public VoteOption findById(String optionId) {
		return voteOptionRepository.findById(optionId)
			.orElseThrow(VoteErrorCode.VOTE_OPTION_NOT_FOUND::toBaseException);
	}

	public List<VoteOption> findByVoteId(String voteId) {
		return voteOptionRepository.findByVoteId(voteId);
	}
}
