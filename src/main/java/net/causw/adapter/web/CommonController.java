package net.causw.adapter.web;

import net.causw.application.CommonService;
import net.causw.application.HomePageService;
import net.causw.application.dto.HomePageResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class CommonController {
    private final HomePageService homePageService;
    private final CommonService commonService;

    public CommonController(
            HomePageService homePageService,
            CommonService commonService
    ) {
        this.homePageService = homePageService;
        this.commonService = commonService;
    }

    @GetMapping("/api/v1/home")
    @ResponseStatus(value = HttpStatus.OK)
    public List<HomePageResponseDto> getHomePage(@AuthenticationPrincipal String userId) {
        return this.homePageService.getHomePage(userId);
    }

    /*
     * Health check for k8s readiness probe
     * */
    @GetMapping("/healthy")
    @ResponseStatus(value = HttpStatus.OK)
    public Map<String, String> healthCheck() {
        HashMap<String, String> map = new HashMap<>();
        map.put("status", "OK");
        return map;
    }

    @PostMapping("/api/v1/flag")
    @ResponseStatus(value = HttpStatus.OK)
    public Boolean createFlag(
            @AuthenticationPrincipal String userId,
            @RequestParam String key,
            @RequestParam Boolean value
    ) {
        return this.commonService.createFlag(
                userId,
                key,
                value
        );
    }

    @PutMapping("/api/v1/flag")
    @ResponseStatus(value = HttpStatus.OK)
    public Boolean updateFlag(
            @AuthenticationPrincipal String userId,
            @RequestParam String key,
            @RequestParam Boolean value
    ) {
        return this.commonService.updateFlag(
                userId,
                key,
                value
        );
    }
}
