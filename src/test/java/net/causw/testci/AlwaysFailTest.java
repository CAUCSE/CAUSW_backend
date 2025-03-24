package net.causw.testci;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class AlwaysFailTest {

    @Test
    void thisTestWillAlwaysFail() {
        Assertions.fail("이 테스트는 무조건 실패합니다.");
    }
}
