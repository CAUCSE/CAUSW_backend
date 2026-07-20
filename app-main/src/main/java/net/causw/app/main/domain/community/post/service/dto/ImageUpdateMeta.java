package net.causw.app.main.domain.community.post.service.dto;

/**
 * 게시글 수정 시 이미지 메타데이터 (서비스 레이어용)
 *
 * @param order            이미지 순서
 * @param type             이미지 타입 (EXISTING: 기존 유지, NEW: 새 업로드)
 * @param url              기존 이미지 URL (type=EXISTING 일 때 사용)
 * @param fileIndex        업로드된 파일 배열의 인덱스 (type=NEW 일 때 사용)
 * @param isRepresentative 대표 이미지 여부
 */
public record ImageUpdateMeta(
	int order,
	Type type,
	String url,
	Integer fileIndex,
	boolean isRepresentative) {

	public enum Type {
		EXISTING,
		NEW
	}
}
