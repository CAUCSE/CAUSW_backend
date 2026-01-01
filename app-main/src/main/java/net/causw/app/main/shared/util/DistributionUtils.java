package net.causw.app.main.shared.util;

import java.util.List;
import java.util.Random;

import net.causw.app.main.shared.seed.ActionType;

public class DistributionUtils {
	private static final Random random = new Random();

	// 게시판 ID 선택 로직 (파레토: 1번 55%, 2번 30%, 나머지 15%)
	public static String selectBoardId(String freeBoardId, String humorBoardId, List<String> otherBoardIds) {
		double p = random.nextDouble();
		if (p < 0.55)
			return freeBoardId; // 실제 자유게시판 ID
		if (p < 0.85)
			return humorBoardId; // 실제 유머게시판 ID

		return otherBoardIds.get(random.nextInt(otherBoardIds.size()));
	}

	// 반환값: 0=Whale, 1=Dolphin, 2=ActiveMinnow
	public static int selectUserGroupType(ActionType actionType) {
		double p = random.nextDouble(); // 0.0 ~ 1.0 난수 생성

		if (p < actionType.whaleThreshold) {
			return 0; // Whale 당첨
		} else if (p < actionType.dolphinThreshold) {
			return 1; // Dolphin 당첨
		} else {
			return 2; // Minnow 당첨
		}
	}

	// 그룹 내에서 누가 활동할지 결정
	public static <T> T pickWeightedRandom(List<T> list, double n) {
		int index = (int)(list.size() * Math.pow(random.nextDouble(), n));
		index = Math.max(0, Math.min(index, list.size() - 1));
		return list.get(index);
	}
}