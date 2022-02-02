package net.causw.adapter.web;

import net.causw.application.HomePageService;
import net.causw.application.dto.HomePageResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class CommonController {

    private final HomePageService homePageService;

    public CommonController(HomePageService homePageService) {
        this.homePageService = homePageService;
    }

    @GetMapping("/api/v1/home")
    @ResponseStatus(value = HttpStatus.OK)
    public List<HomePageResponseDto> getHomePage(@AuthenticationPrincipal String userId) {
        return this.homePageService.getHomePage(userId);
    }

    /*
     * Health check for k8s readiness probe
     * */
    @GetMapping("/api/v1/healthy")
    @ResponseStatus(value = HttpStatus.OK)
    public Map<String, String> healthCheck() {
        HashMap<String, String> map = new HashMap<>();
        map.put("status", "OK");
        return map;
    }
}
