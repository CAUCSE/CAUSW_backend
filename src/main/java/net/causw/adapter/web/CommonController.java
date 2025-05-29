package net.causw.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import net.causw.application.common.CommonService;
import net.causw.application.homepage.HomePageService;
import net.causw.application.dto.homepage.HomePageResponseDto;
import net.causw.config.security.userdetails.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequiredArgsConstructor
@RequestMapping
public class CommonController {
    private final HomePageService homePageService;
    private final CommonService commonService;

    @GetMapping("/api/v1/home")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "홈페이지 불러오기 API(완료)",
            description = "동아리에 속하지 않고 삭제되지 않은 게시판과 해당 게시판의 최신 글 3개의 정보를 반환합니다.\n" +
                    "개발 db상에는 동아리에 속하지 않은 많은 더미 데이터가 있지만 실제 운영될 때는 동아리에 속하지 않는 게시판은 학생회 공지게시판 뿐입니다.")
    public List<HomePageResponseDto> getHomePage(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return this.homePageService.getHomePage(userDetails.getUser());
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
    @PreAuthorize("@security.hasRole(@Role.ADMIN)")
    public Boolean createFlag(
            @RequestParam("key") String key,
            @RequestParam("value") Boolean value
    ) {
        return commonService.createFlag(
                key,
                value
        );
    }

    @PutMapping("/api/v1/flag")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRole(@Role.ADMIN)")
    public Boolean updateFlag(
            @RequestParam("key") String key,
            @RequestParam("value") Boolean value
    ) {

        return this.commonService.updateFlag(
                key,
                value
        );
    }
}
