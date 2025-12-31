package net.causw.app.main.shared.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.causw.app.main.shared.util.DistributionUtils;
import net.causw.app.main.shared.util.UserSegmenter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public abstract class BasePostSeeder<T> {
    protected final JdbcTemplate jdbcTemplate;
    protected final UserSegmenter userSegmenter; // 유저 유틸 상속
    protected final int totalCount;
    protected final int batchSize;

    // [Template Method] 실행 흐름을 제어 (final로 오버라이드 방지를 하려고 했으나, AOP 프록시 객체와의 충돌로 제거함)
    @Transactional
    public void seed() {
        log.info("[Seeder] Start seeding {} items for {}", totalCount, this.getClass().getSimpleName());
        beforeSeeding();
        long startTime = System.currentTimeMillis();

        List<T> buffer = new ArrayList<>();

        for (int i = 0; i < totalCount; i++) {
            // 1. 데이터 생성 위임 (Hook Method)
            T item = createItem(i);
            buffer.add(item);

            // 2. 배치가 차면 DB 투입
            if (buffer.size() >= batchSize) {
                batchInsert(buffer);
                buffer.clear();
            }
        }

        // 3. 남은 데이터 처리
        if (!buffer.isEmpty()) {
            batchInsert(buffer);
        }

        long endTime = System.currentTimeMillis();
        log.info("[Seeder] Finished. Time taken: {}ms", endTime - startTime);
    }

    protected String pickUser() {
        ActionType type = getActionType();
        int groupType = DistributionUtils.selectUserGroupType(type);

        // UserSegmenter 내부도 List<String>으로 관리되고 있다고 가정
        List<String> targetGroup = switch (groupType) {
            case 0 -> userSegmenter.getWhales();
            case 1 -> userSegmenter.getDolphins();
            default -> userSegmenter.getActiveMinnows();
        };

        return DistributionUtils.pickWeightedRandom(targetGroup, type.skewFactor);
    }

    // [Abstract Methods] 자식들이 반드시 구현해야 할 부분
    protected abstract T createItem(int index); // 데이터 생성 전략
    protected abstract void batchInsert(List<T> items); // DB 저장 전략
    protected abstract ActionType getActionType(); // 어떤 확률 분포를 쓸지
    protected void beforeSeeding() {}
}