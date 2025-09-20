package net.causw.app.main.repository.circle.query;

import java.util.List;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.circle.Circle;
import net.causw.app.main.domain.model.entity.circle.QCircle;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CircleQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<Circle> findCirclesByLeaderIds(List<String> leaderUserIds) {
		QCircle circle = QCircle.circle;

		return jpaQueryFactory
			.selectFrom(circle)
			.where(circle.leader.id.in(leaderUserIds))
			.fetch();
	}
}
