package net.causw.app.main.shared.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "페이지네이션 응답")
public class PageResponse<T> {

	@Schema(description = "데이터 목록")
	private List<T> content;

	@Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
	private int currentPage;

	@Schema(description = "페이지 크기", example = "10")
	private int size;

	@Schema(description = "전체 페이지 수", example = "5")
	private int totalPages;

	@Schema(description = "전체 요소 수", example = "50")
	private long totalElements;

	@Schema(description = "다음 페이지 존재 여부", example = "true")
	private boolean hasNext;

	@Schema(description = "이전 페이지 존재 여부", example = "false")
	private boolean hasPrev;

	public static <T> PageResponse<T> from(Page<T> page) {
		return new PageResponse<>(
			page.getContent(),
			page.getNumber(),
			page.getSize(),
			page.getTotalPages(),
			page.getTotalElements(),
			page.hasNext(),
			page.hasPrevious());
	}
}