package net.causw.app.main.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import net.causw.app.main.dto.homepage.HomePageResponseDto;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.app.main.service.common.CommonService;
import net.causw.app.main.service.homepage.HomePageService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class CommonController {
	private final HomePageService homePageService;
	private final CommonService commonService;

	@GetMapping("/api/v1/home")
	@ResponseStatus(value = HttpStatus.OK)
	@PreAuthorize("!@security.isGraduatedUser()")
	@Operation(summary = "홈페이지 불러오기 API(완료)",
		description = "동아리에 속하지 않고 삭제되지 않은 게시판과 해당 게시판의 최신 글 3개의 정보를 반환합니다.\n" +
			"개발 db상에는 동아리에 속하지 않은 많은 더미 데이터가 있지만 실제 운영될 때는 동아리에 속하지 않는 게시판은 학생회 공지게시판 뿐입니다.\n" +
			"졸업생은 해당 api에 접근이 불가합니다."
	)
	public List<HomePageResponseDto> getHomePage(@AuthenticationPrincipal CustomUserDetails userDetails) {

		return this.homePageService.getHomePage(userDetails.getUser());
	}

	@GetMapping("/api/v1/home/alumni")
	@ResponseStatus(value = HttpStatus.OK)
	@PreAuthorize("@security.isGraduatedUser()")
	@Operation(summary = "크자회 전용 홈페이지 불러오기 API(완료)",
		description = "크자회 전용 홈페이지에 보여질 크자회 공지 게시판, 소통 게시판을 반환하기 위한 api 입니다.\n" +
			"db상에 isAlumni, isHome 값이 모두 true 인 경우를 반환합니다.")
	public List<HomePageResponseDto> getAlumniHomePage(@AuthenticationPrincipal CustomUserDetails userDetails) {

		return this.homePageService.getAlumniHomePage(userDetails.getUser());
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
