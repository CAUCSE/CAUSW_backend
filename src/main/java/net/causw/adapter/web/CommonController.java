package net.causw.adapter.web;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import net.causw.application.common.CommonService;
import net.causw.application.homepage.HomePageService;
import net.causw.application.dto.homepage.HomePageResponseDto;
import org.springframework.http.HttpStatus;
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
@RequiredArgsConstructor
@RequestMapping
public class CommonController {
    private final HomePageService homePageService;
    private final CommonService commonService;

    @GetMapping("/api/v1/home")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "홈페이지 불러오기 API(완료)", notes = "동아리에 속하지 않고 삭제되지 않은 게시판과 해당 게시판의 최신 글 3개의 정보를 반환합니다. \n 개발 db상에는 동아리에 속하지 않은 많은 더미 데이터가 있지만 실제 운영될 때는 동아리에 속하지 않는 게시판은 학생회 공지게시판 뿐입니다.")
    public List<HomePageResponseDto> getHomePage() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.homePageService.getHomePage(loginUserId);
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
