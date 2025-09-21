package net.causw.app.main.service.circle;

import java.util.List;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.model.entity.circle.Circle;
import net.causw.app.main.repository.circle.query.CircleQueryRepository;

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
