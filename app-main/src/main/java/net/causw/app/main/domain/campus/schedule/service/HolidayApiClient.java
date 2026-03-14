package net.causw.app.main.domain.campus.schedule.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HolidayApiClient {

	private static final DateTimeFormatter LOC_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
	private static final int DEFAULT_NUM_OF_ROWS = 100;

	private final RestClient restClient;
	private final String serviceKey;

	public HolidayApiClient(RestClient.Builder restClientBuilder,
		@Value("${app.holiday-api.base-url:http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService}") String baseUrl,
		@Value("${app.holiday-api.service-key:}") String serviceKey) {
		this.restClient = restClientBuilder
			.baseUrl(baseUrl)
			.build();
		this.serviceKey = serviceKey;
	}

	public List<HolidayInfo> fetchHolidaysByYear(int year) {
		if (!StringUtils.hasText(serviceKey)) {
			log.warn("공휴일 API service-key가 비어 있어 조회를 건너뜁니다.");
			return List.of();
		}

		List<HolidayInfo> holidays = new ArrayList<>();
		int pageNo = 1;
		while (true) {
			HolidayPageResponse pageResponse = holidayRequest(year, pageNo);
			pageResponse.items().stream()
				.filter(item -> "Y".equalsIgnoreCase(item.isHoliday()))
				.filter(item -> Objects.nonNull(item.locdate()) && Objects.nonNull(item.dateName()))
				.map(item -> new HolidayInfo(
					LocalDate.parse(item.locdate(), LOC_DATE_FORMATTER),
					item.dateName()))
				.forEach(holidays::add);

			if (pageResponse.items().isEmpty() || (long)pageNo * DEFAULT_NUM_OF_ROWS >= pageResponse.totalCount()) {
				break;
			}
			pageNo++;
		}

		return Collections.unmodifiableList(holidays);
	}

	private HolidayPageResponse holidayRequest(int year, int pageNo) {
		HolidayApiResponse response = restClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/getRestDeInfo")
				.queryParam("ServiceKey", serviceKey)
				.queryParam("pageNo", pageNo)
				.queryParam("numOfRows", DEFAULT_NUM_OF_ROWS)
				.queryParam("solYear", year)
				.queryParam("_type", "json")
				.build())
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(HolidayApiResponse.class);

		if (response == null || response.response() == null || response.response().body() == null
			|| response.response().body().items() == null || response.response().body().items().item() == null) {
			return HolidayPageResponse.empty();
		}

		String resultCode = response.response().header() != null ? response.response().header().resultCode() : "";
		if (!"00".equals(resultCode)) {
			String resultMsg = response.response().header() != null ? response.response().header().resultMsg() : "";
			log.warn("공휴일 API 비정상 응답: resultCode={}, resultMsg={}, year={}, pageNo={}", resultCode, resultMsg, year, pageNo);
			return HolidayPageResponse.empty();
		}

		int totalCount = response.response().body().totalCount() == null ? 0 : response.response().body().totalCount();
		return new HolidayPageResponse(response.response().body().items().item(), totalCount);
	}

	private record HolidayPageResponse(List<HolidayItemResponse> items, int totalCount) {
		private static HolidayPageResponse empty() {
			return new HolidayPageResponse(List.of(), 0);
		}
	}

	public record HolidayInfo(LocalDate date, String name) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record HolidayApiResponse(ApiResponsePayload response) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record ApiResponsePayload(HeaderResponse header, BodyResponse body) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record HeaderResponse(String resultCode, String resultMsg) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record BodyResponse(ItemsResponse items, Integer totalCount, Integer numOfRows, Integer pageNo) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record ItemsResponse(
		@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) List<HolidayItemResponse> item) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record HolidayItemResponse(String locdate, String dateName, String isHoliday) {
	}
}





