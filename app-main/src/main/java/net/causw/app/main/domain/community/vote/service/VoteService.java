package net.causw.app.main.domain.community.vote.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.community.vote.api.v2.dto.request.CreateVoteRequest;
import net.causw.app.main.domain.community.vote.api.v2.dto.response.VoteOptionResponse;
import net.causw.app.main.domain.community.vote.api.v2.dto.response.VoteResponse;
import net.causw.app.main.domain.community.vote.entity.Vote;
import net.causw.app.main.domain.community.vote.entity.VoteOption;
import net.causw.app.main.domain.community.vote.entity.VoteRecord;
import net.causw.app.main.domain.community.vote.service.implementation.VoteOptionReader;
import net.causw.app.main.domain.community.vote.service.implementation.VoteOptionWriter;
import net.causw.app.main.domain.community.vote.service.implementation.VoteReader;
import net.causw.app.main.domain.community.vote.service.implementation.VoteRecordReader;
import net.causw.app.main.domain.community.vote.service.implementation.VoteRecordWriter;
import net.causw.app.main.domain.community.vote.service.implementation.VoteWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.errorcode.VoteErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoteService {

	private final PostReader postReader;
	private final VoteReader voteReader;
	private final VoteWriter voteWriter;
	private final VoteOptionReader voteOptionReader;
	private final VoteOptionWriter voteOptionWriter;
	private final VoteRecordReader voteRecordReader;
	private final VoteRecordWriter voteRecordWriter;

	@Transactional
	public VoteResponse createVote(CreateVoteRequest request, User user) {
		Post post = postReader.findById(request.getPostId());

		if (!user.equals(post.getWriter())) {
			throw VoteErrorCode.VOTE_CREATE_NOT_ALLOWED.toBaseException();
		}

		List<VoteOption> voteOptions = request.getOptions().stream()
			.map(VoteOption::of)
			.collect(Collectors.toList());

		Vote vote = Vote.of(
			request.getTitle(),
			request.getAllowAnonymous(),
			request.getAllowMultiple(),
			voteOptions,
			post);
		Vote savedVote = voteWriter.save(vote);
		post.updateVote(savedVote);
		voteOptions.forEach(o -> o.updateVote(savedVote));
		voteOptionWriter.saveAll(voteOptions);

		return buildVoteResponse(savedVote, user);
	}

	/**
	 * 옵션 토글.
	 * - 같은 옵션 재클릭 → 취소 (allowMultiple 무관)
	 * - 다른 옵션 + allowMultiple=false → 기존 선택 전부 삭제 후 추가
	 * - 다른 옵션 + allowMultiple=true  → 그냥 추가
	 */
	@Transactional
	public VoteResponse toggleVote(String voteId, String optionId, User user) {
		Vote vote = voteReader.findById(voteId);

		if (vote.isEnd()) {
			throw VoteErrorCode.VOTE_ALREADY_END.toBaseException();
		}

		VoteOption voteOption = voteOptionReader.findById(optionId);

		if (!voteOption.getVote().getId().equals(voteId)) {
			throw VoteErrorCode.VOTE_OPTION_NOT_IN_VOTE.toBaseException();
		}

		Optional<VoteRecord> existing = voteRecordReader.findByVoteOptionAndUser(voteOption, user);

		if (existing.isPresent()) {
			// 같은 옵션 재클릭 → 취소
			voteRecordWriter.deleteByVoteOptionAndUser(voteOption, user);
		} else {
			if (!vote.isAllowMultiple()) {
				// allowMultiple=false: 기존 선택 전부 제거 후 새 선택 추가.
				// SELECT-then-DELETE 동작. 벌크 삭제 아님. 기존 선택은 항상 1건 이하.
				voteRecordWriter.deleteAllByVoteAndUser(vote, user);
			}
			voteRecordWriter.save(VoteRecord.of(user, voteOption));
		}

		return buildVoteResponse(vote, user);
	}

	@Transactional
	public VoteResponse endVote(String voteId, User user) {
		Vote vote = voteReader.findById(voteId);

		if (!user.equals(vote.getPost().getWriter())) {
			throw VoteErrorCode.VOTE_END_NOT_ALLOWED.toBaseException();
		}
		if (vote.isEnd()) {
			throw VoteErrorCode.VOTE_ALREADY_END.toBaseException();
		}

		vote.endVote();
		voteWriter.save(vote);
		return buildVoteResponse(vote, user);
	}

	@Transactional
	public VoteResponse restartVote(String voteId, User user) {
		Vote vote = voteReader.findById(voteId);

		if (!user.equals(vote.getPost().getWriter())) {
			throw VoteErrorCode.VOTE_RESTART_NOT_ALLOWED.toBaseException();
		}
		if (!vote.isEnd()) {
			throw VoteErrorCode.VOTE_NOT_END.toBaseException();
		}

		vote.restartVote();
		voteWriter.save(vote);
		return buildVoteResponse(vote, user);
	}

	@Transactional(readOnly = true)
	public VoteResponse findVoteById(String voteId, User user) {
		return buildVoteResponse(voteReader.findById(voteId), user);
	}

	@Transactional(readOnly = true)
	public VoteResponse findVoteByPostId(String postId, User user) {
		return buildVoteResponse(voteReader.findByPostId(postId), user);
	}

	private VoteResponse buildVoteResponse(Vote vote, User user) {
		// 투표의 모든 기록을 한 번에 조회 (N+1 방지)
		List<VoteRecord> allRecords = voteRecordReader.findAllByVote(vote);

		// 옵션별 그룹화 (메모리에서)
		Map<String, List<VoteRecord>> recordsByOptionId = allRecords.stream()
			.collect(Collectors.groupingBy(r -> r.getVoteOption().getId()));

		// 요청자가 선택한 옵션 ID 집합
		Set<String> votedOptionIds = allRecords.stream()
			.filter(r -> r.getUser().equals(user))
			.map(r -> r.getVoteOption().getId())
			.collect(Collectors.toSet());

		// 옵션 목록 (생성순 정렬)
		List<VoteOption> options = vote.getVoteOptions()
			.stream()
			.sorted(Comparator.comparing(VoteOption::getCreatedAt))
			.toList();

		// 고유 투표자 수
		Set<String> uniqueVoterIds = allRecords.stream()
			.map(r -> r.getUser().getId())
			.collect(Collectors.toSet());

		// 옵션별 응답 빌드
		List<VoteOptionResponse> optionResponses = new ArrayList<>();
		for (int i = 0; i < options.size(); i++) {
			VoteOption option = options.get(i);
			List<VoteRecord> records = recordsByOptionId.getOrDefault(option.getId(), List.of());

			List<String> voteUsers = vote.isAllowAnonymous()
				? List.of()
				: records.stream().map(r -> r.getUser().getId()).toList();

			optionResponses.add(VoteOptionResponse.of(
				option,
				i,
				records.size(),
				votedOptionIds.contains(option.getId()),
				voteUsers));
		}

		return VoteResponse.of(
			vote,
			vote.getPost().getWriter().equals(user),
			!votedOptionIds.isEmpty(),
			allRecords.size(),
			uniqueVoterIds.size(),
			optionResponses);
	}
}
