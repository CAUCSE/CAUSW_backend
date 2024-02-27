package net.causw.adapter.web;

import net.causw.application.common.CommonService;
import net.causw.application.homepage.HomePageService;
import net.causw.application.dto.homepage.HomePageResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
            @RequestParam String key,
            @RequestParam Boolean value
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.commonService.createFlag(
                loginUserId,
                key,
                value
        );
    }

    @PutMapping("/api/v1/flag")
    @ResponseStatus(value = HttpStatus.OK)
    public Boolean updateFlag(
            @RequestParam String key,
            @RequestParam Boolean value
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.commonService.updateFlag(
                loginUserId,
                key,
                value
        );
    }
}
