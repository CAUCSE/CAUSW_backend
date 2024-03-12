package net.causw.application.circle;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CircleServiceTest {

    @Test
    @DisplayName("Junit 테스트를 활성화합니다.")
    void junitTest() {
        String test = "동문 네트워크";
        Assertions.assertEquals("동문 네트워크", test);
    }
}