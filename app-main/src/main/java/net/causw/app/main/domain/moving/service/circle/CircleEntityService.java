package net.causw.app.main.domain.moving.service.circle;

import java.util.List;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.moving.model.entity.circle.Circle;
import net.causw.app.main.domain.moving.repository.circle.query.CircleQueryRepository;

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
