package net.causw;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = JunitTest.class)
public class JunitTest {

    @Test
    @DisplayName("Junit 테스트를 활성화합니다.")
    void junitTest() {
        String test = "동문 네트워크";
        Assertions.assertEquals("동문 네트워크", test);
    }
}
