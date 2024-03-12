package net.causw.adapter.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BoardControllerTest {

    @Test
    @DisplayName("Junit 테스트를 수행합니다.")
    void junitTest() {
        String test = "동문 네트워크";
        Assertions.assertEquals("동문 네트워크", test);
    }
}