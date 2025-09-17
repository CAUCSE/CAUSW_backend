package net.causw.app.main.repository.circle.query;

import java.util.List;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.circle.Circle;
import net.causw.app.main.domain.model.entity.circle.QCircle;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CircleQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	// Interface 프로젝션 (더 유연한 방식)
	public interface CircleLeaderProjection {
		String getId();
		String getLeaderId();
	}

	// Interface 프로젝션 사용 (QueryDSL에서 직접 지원)
	public List<CircleLeaderProjection> findCircleLeaderProjectionByLeaderIds(List<String> leaderUserIds) {
		QCircle circle = QCircle.circle;

		return jpaQueryFactory
			.select(Projections.fields(CircleLeaderProjection.class,
				circle.id.as("circleId"),
				circle.leader.id.as("leaderId")
			))
			.from(circle)
			.where(circle.leader.id.in(leaderUserIds))
			.fetch();
	}
}
