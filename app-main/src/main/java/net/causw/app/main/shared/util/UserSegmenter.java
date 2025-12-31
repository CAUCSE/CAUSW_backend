package net.causw.app.main.shared.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSegmenter {
    private final UserRepository userRepository;

    // 캐싱된 ID 그룹들
    private List<String> whaleIds;       // 1%
    private List<String> dolphinIds;     // 9%
    private List<String> activeMinnowIds;// 45% (90% 중 50%)
    // 나머지 45퍼센트는 데이터 생성에 참여하지 않음

    @PostConstruct
    public void init() {
        // 1. 전체 ID 로드 (성능을 위해 ID만 조회)
        List<String> allIds = userRepository.findAllIds();
        Collections.shuffle(allIds); // 무작위성을 위해 섞기

        int total = allIds.size();
        int whaleCount = (int) (total * 0.01);
        int dolphinCount = (int) (total * 0.09);
        int activeMinnowCount = (int) (total * 0.9 * 0.5); // 90% 중 절반

        int current = 0;
        this.whaleIds = allIds.subList(current, current + whaleCount);
        current += whaleCount;

        this.dolphinIds = allIds.subList(current, current + dolphinCount);
        current += dolphinCount;

        this.activeMinnowIds = allIds.subList(current, current + activeMinnowCount);

        log.info("User Segmentation Complete: Whales=" + whaleIds.size());
    }

    public List<String> getWhales() { return whaleIds; }
    public List<String> getDolphins() { return dolphinIds; }
    public List<String> getActiveMinnows() { return activeMinnowIds; }
}
