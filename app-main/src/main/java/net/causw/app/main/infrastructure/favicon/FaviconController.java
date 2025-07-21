package net.causw.app.main.infrastructure.favicon;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// https://www.baeldung.com/spring-boot-favicon
@Controller
class FaviconController {

    @GetMapping("/favicon.ico")
    void returnNoFavicon() {
    }
}
