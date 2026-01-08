package net.causw.app.main.shared.util;

import java.util.TreeMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import net.causw.app.main.shared.seed.PostMetaData;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HotPostSampler {

	private final JdbcTemplate jdbcTemplate;

	// 누적 확률 분포를 저장할 맵 (빠른 검색용)
	private final TreeMap<Double, PostMetaData> cumulativeDistribution = new TreeMap<>();
	private double totalWeight = 0.0;
	private boolean isInitialized = false;

	// 시딩 시작 전(beforeSeeding)에 딱 한 번 호출
	public void init() {
		if (isInitialized)
			return;

		// 1. 모든 게시글 ID와 보드 정보 조회
		String sql = """
			    SELECT p.id, p.board_id, p.user_id, b.name as board_name
			    FROM tb_board b
			    JOIN tb_post p ON p.board_id = b.id
			""";

		jdbcTemplate.query(sql, rs -> {
			String id = rs.getString("id");
			String boardName = rs.getString("board_name");
			String userId = rs.getString("user_id");

			// 2. 바이럴 점수 계산
			double score = calculateViralScore(boardName);

			PostMetaData meta = new PostMetaData(id, rs.getString("board_id"), score);

			// 3. 가중치 누적
			totalWeight += score;
			cumulativeDistribution.put(totalWeight, meta);
		});

		isInitialized = true;
	}

	// 게시글의 바이럴 정도 결정 로직
	private double calculateViralScore(String boardName) {
		double baseScore = 1.0;

		// 사람이 많은 곳에 쓴 글이 주목받을 가능성이 높다.
		if ("자유게시판".equals(boardName))
			baseScore *= 5.0;
		else if ("유머게시판".equals(boardName))
			baseScore *= 3.0;
		else
			baseScore *= 0.1;

		// Math.random()은 0~1 사이. 역수 등을 취해 롱테일 분포 생성
		// 값이 작을수록 빈도가 높고, 클수록 빈도가 낮지만 엄청 큼
		double luck = Math.pow(Math.random(), -0.5); // 파레토 분포와 유사한 효과

		return baseScore * luck;
	}

	// 가중치 랜덤 뽑기 (점수 높은 글이 더 자주 리턴됨)
	public PostMetaData pickHotPost() {
		if (!isInitialized)
			init();

		double randomValue = Math.random() * totalWeight;
		return cumulativeDistribution.higherEntry(randomValue).getValue();
	}
}