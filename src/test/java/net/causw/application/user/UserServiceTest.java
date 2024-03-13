package net.causw.application.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Test
    @DisplayName("Junit을 활성화합니다.")
    void Junit을_활성화합니다() {
        String test = "동문 네트워크";
        Assertions.assertEquals("동문 네트워크", test);
    }

}