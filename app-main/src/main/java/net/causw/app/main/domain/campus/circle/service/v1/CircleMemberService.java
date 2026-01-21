package net.causw.app.main.domain.campus.circle.service.v1;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import net.causw.app.main.core.aop.annotation.MeasureTime;
import net.causw.app.main.domain.campus.circle.entity.Circle;
import net.causw.app.main.domain.campus.circle.entity.CircleMember;
import net.causw.app.main.domain.campus.circle.enums.CircleMemberStatus;
import net.causw.app.main.domain.campus.circle.repository.CircleMemberRepository;
import net.causw.app.main.domain.community.form.entity.Reply;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@MeasureTime
@Service
@RequiredArgsConstructor
public class CircleMemberService {

	private final CircleMemberRepository circleMemberRepository;

	public Optional<CircleMember> findById(String id) {
		return this.circleMemberRepository.findById(id);
	}

	public List<CircleMember> findByUserId(String userId) {
		return this.circleMemberRepository.findByUser_Id(userId);
	}

	public Map<String, CircleMember> findCircleByUserId(String userId) {
		return this.circleMemberRepository.findByUser_Id(userId)
			.stream()
			.filter(circleMember -> circleMember.getStatus().equals(CircleMemberStatus.MEMBER))
			.collect(Collectors.toMap(
				circleMember -> circleMember.getCircle().getId(),
				circleMember -> circleMember));
	}

	public List<CircleMember> getCircleListByUserId(String userId) {
		return this.circleMemberRepository.findByUser_Id(userId)
			.stream()
			.filter(circleMember -> circleMember.getStatus() == CircleMemberStatus.MEMBER)
			.collect(Collectors.toList());
	}

	public List<CircleMember> findByCircleId(String circleId, CircleMemberStatus status) {
		return this.circleMemberRepository.findByCircle_Id(circleId)
			.stream()
			.filter(circleMember -> circleMember.getStatus() == status)
			.collect(Collectors.toList());
	}

	public Optional<CircleMember> findByUserIdAndCircleId(String userId, String circleId) {
		return this.circleMemberRepository.findByUser_IdAndCircle_Id(userId, circleId);
	}

	public Long getNumMember(String id) {
		return this.circleMemberRepository.getNumMember(id);
	}

	public CircleMember create(User user, Circle circle, Reply reply) {
		return this.circleMemberRepository.save(CircleMember.of(
			circle,
			user,
			reply.getForm(),
			reply));
	}

	public Optional<CircleMember> updateStatus(String applicationId, CircleMemberStatus targetStatus) {
		return this.circleMemberRepository.findById(applicationId).map(
			circleMember -> {
				circleMember.setStatus(targetStatus);

				return this.circleMemberRepository.save(circleMember);
			});
	}
}
