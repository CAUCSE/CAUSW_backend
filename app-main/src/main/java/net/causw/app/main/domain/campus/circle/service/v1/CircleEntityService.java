package net.causw.app.main.domain.campus.circle.service.v1;

import java.util.List;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.campus.circle.entity.Circle;
import net.causw.app.main.domain.campus.circle.repository.query.CircleQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CircleEntityService {

	private final CircleQueryRepository circleQueryRepository;

	public List<Circle> findCirclesByLeaderIds(List<String> leaderUserIds) {

		return circleQueryRepository
			.findCirclesByLeaderIds(leaderUserIds);
	}
}
