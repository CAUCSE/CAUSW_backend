package net.causw.app.main.dto.circle;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CircleResponseDto {

	@Schema(description = "동아리 ID", example = "UUID 형식의 동아리 고유 ID String 값 입니다.")
	private String id;

	@Schema(description = "동아리 이름", example = "소프트웨어학부 특별기구 ICT위원회 동문네트워크")
	private String name;

	@Schema(description = "동아리 메인 이미지(nullable)", example = "String")
	private String mainImage;

	@Schema(description = "동아리 설명", example = "ICT위원회는 동문 네트워크 서비스를 만드는 특별기구이자 동아리입니다.")
	private String description;

	@Schema(description = "동아리 삭제 여부", example = "false")
	private Boolean isDeleted;

	@Schema(description = "동아리장 ID", example = "UUID 형식의 동아리장 고유 ID 값 String 입니다.")
	private String leaderId;

	@Schema(description = "동아리장 이름", example = "정상제")
	private String leaderName;

	@Schema(description = "동아리 회비", example = "5000")
	private Integer circleTax;

	@Schema(description = "동아리 모집인원", example = "10")
	private Integer recruitMembers;

	@Schema(description = "동아리원 숫자", example = "7")
	private Long numMember;

	@Schema(description = "동아리 생성 일시", example = "2024-02-04T16:11:02.342644")
	private LocalDateTime createdAt;

	@Schema(description = "동아리 모집 종료 날짜", example = "2024-10-10")
	private LocalDateTime recruitEndDate;

	@Schema(description = "동아리 모집 여부", example = "false")
	private Boolean isRecruit;

}
