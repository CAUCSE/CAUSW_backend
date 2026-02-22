package net.causw.app.main.domain.community.ceremony.service.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;
import net.causw.app.main.domain.community.ceremony.service.CeremonyAdminService;
import net.causw.app.main.domain.community.ceremony.service.dto.CeremonyAdminListCondition;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyReader;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;

@ExtendWith(MockitoExtension.class)
class CeremonyAdminServiceTest {

	@InjectMocks
	CeremonyAdminService ceremonyAdminService;

	@Mock
	CeremonyReader ceremonyReader;

	@Nested
	@DisplayName("관리자 경조사 목록 조회 테스트")
	class GetCeremonyListTest {

		final Pageable pageable = PageRequest.of(0, 10);

		@Test
		@DisplayName("모든 조건이 있을 때 reader에 올바르게 전달되고 결과를 반환한다")
		void givenAllConditions_whenGetCeremonyList_thenReturnsPage() {
			// given
			LocalDate fromDate = LocalDate.of(2026, 1, 1);
			LocalDate toDate = LocalDate.of(2026, 3, 31);
			CeremonyState state = CeremonyState.AWAIT;
			CeremonyAdminListCondition condition = new CeremonyAdminListCondition(fromDate, toDate, state);

			Ceremony c1 = mock(Ceremony.class);
			Ceremony c2 = mock(Ceremony.class);
			Page<Ceremony> ceremonyPage = new PageImpl<>(List.of(c1, c2));

			given(ceremonyReader.findAllForAdmin(fromDate, toDate, state, pageable))
				.willReturn(ceremonyPage);

			// when
			Page<Ceremony> result = ceremonyAdminService.getCeremonyList(condition, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(2);
			assertThat(result.getContent()).containsExactly(c1, c2);

			then(ceremonyReader).should(times(1))
				.findAllForAdmin(fromDate, toDate, state, pageable);
		}

		@Test
		@DisplayName("조건이 모두 null이어도 reader에 null로 전달되어 전체 조회된다")
		void givenNoConditions_whenGetCeremonyList_thenPassesNullsToReader() {
			// given
			CeremonyAdminListCondition condition = new CeremonyAdminListCondition(null, null, null);
			Page<Ceremony> emptyPage = Page.empty(pageable);

			given(ceremonyReader.findAllForAdmin(null, null, null, pageable))
				.willReturn(emptyPage);

			// when
			Page<Ceremony> result = ceremonyAdminService.getCeremonyList(condition, pageable);

			// then
			assertThat(result.getTotalElements()).isZero();

			then(ceremonyReader).should(times(1))
				.findAllForAdmin(null, null, null, pageable);
		}

		@Test
		@DisplayName("일부 조건만 있을 때도 올바르게 전달된다")
		void givenPartialConditions_whenGetCeremonyList_thenPassesCorrectly() {
			// given
			LocalDate fromDate = LocalDate.of(2026, 1, 1);
			CeremonyAdminListCondition condition = new CeremonyAdminListCondition(fromDate, null, CeremonyState.ACCEPT);

			Ceremony c1 = mock(Ceremony.class);
			Page<Ceremony> ceremonyPage = new PageImpl<>(List.of(c1));

			given(ceremonyReader.findAllForAdmin(fromDate, null, CeremonyState.ACCEPT, pageable))
				.willReturn(ceremonyPage);

			// when
			Page<Ceremony> result = ceremonyAdminService.getCeremonyList(condition, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(1);

			then(ceremonyReader).should(times(1))
				.findAllForAdmin(fromDate, null, CeremonyState.ACCEPT, pageable);
		}
	}

	@Nested
	@DisplayName("관리자 경조사 상세 조회 테스트")
	class GetCeremonyDetailTest {

		@Test
		@DisplayName("존재하는 경조사를 조회하면 Ceremony를 반환한다")
		void givenExistingCeremonyId_whenGetDetail_thenReturnsCeremony() {
			// given
			Ceremony ceremony = mock(Ceremony.class);
			given(ceremonyReader.findById("ceremonyId")).willReturn(Optional.of(ceremony));

			// when
			Ceremony result = ceremonyAdminService.getCeremonyDetail("ceremonyId");

			// then
			assertThat(result).isSameAs(ceremony);

			then(ceremonyReader).should(times(1)).findById("ceremonyId");
		}

		@Test
		@DisplayName("존재하지 않는 경조사를 조회하면 CEREMONY_NOT_FOUND 예외를 던진다")
		void givenNonExistingCeremonyId_whenGetDetail_thenThrowsException() {
			// given
			given(ceremonyReader.findById("nonExistId")).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> ceremonyAdminService.getCeremonyDetail("nonExistId"))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessageContaining(CeremonyErrorCode.CEREMONY_NOT_FOUND.getMessage())
				.extracting("errorCode")
				.isEqualTo(CeremonyErrorCode.CEREMONY_NOT_FOUND);

			then(ceremonyReader).should(times(1)).findById("nonExistId");
		}
	}
}
