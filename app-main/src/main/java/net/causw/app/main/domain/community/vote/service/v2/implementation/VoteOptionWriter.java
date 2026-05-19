package net.causw.app.main.domain.community.vote.service.v2.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.vote.entity.VoteOption;
import net.causw.app.main.domain.community.vote.repository.VoteOptionRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class VoteOptionWriter {
	private final VoteOptionRepository voteOptionRepository;

	public List<VoteOption> saveAll(List<VoteOption> voteOptions) {
		return voteOptionRepository.saveAll(voteOptions);
	}
}
