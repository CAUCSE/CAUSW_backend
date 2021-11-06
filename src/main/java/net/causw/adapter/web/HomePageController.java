package net.causw.adapter.web;

import net.causw.application.HomePageService;
import net.causw.application.dto.HomePageResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/home")
public class HomePageController {
    private final HomePageService homePageService;

    public HomePageController(HomePageService homePageService) {
        this.homePageService = homePageService;
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public List<HomePageResponseDto> getHomePage(@AuthenticationPrincipal String userId) {
        return this.homePageService.getHomePage(userId);
    }
}
