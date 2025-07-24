package net.causw.app.main.domain.model.enums.chat;

public enum MessageType {
	// 사용자가 입력하는 순수 텍스트
	TEXT,

	//업로드된 이미지의 단순 URL 주소
	IMAGE,

	//업로드된 파일의 단순 URL 주소
	FILE,

	//이벤트의 구조화된 정보 (JSON 형식의 문자열)
	SYSTEM
}