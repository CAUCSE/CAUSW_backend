package net.causw.app.main.domain.community.post.service.dto;

/**
 * 게시글 생성 시 이미지 메타데이터 (서비스 레이어용)
 *
 * @param order            이미지 순서
 * @param fileIndex        업로드된 파일 배열의 인덱스
 * @param isRepresentative 대표 이미지 여부
 */
public record ImageCreateMeta(
	int order,
	int fileIndex,
	boolean isRepresentative) {
}
