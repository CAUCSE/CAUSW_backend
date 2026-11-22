package net.causw.app.main.domain.notification.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.notification.notification.entity.UserBoardSubscribe;
import net.causw.app.main.domain.notification.notification.entity.UserNotificationSetting;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UserBoardSubscribeQueryRepositoryTest.JpaTestConfig.class)
@Transactional
class UserBoardSubscribeQueryRepositoryTest {

	@Autowired
	private EntityManager entityManager;

	private UserBoardSubscribeQueryRepository userBoardSubscribeQueryRepository;
	private Board board;
	private int userSequence;

	@BeforeEach
	void setUp() {
		userBoardSubscribeQueryRepository = new UserBoardSubscribeQueryRepository(new JPAQueryFactory(entityManager));
		board = Board.createForV2("학생회", "소식 게시판");
		entityManager.persist(board);
	}

	@Nested
	@DisplayName("소식 글 알림 대상 조회 (findNotificationTargets)")
	class FindNotificationTargetsTest {

		@Test
		@DisplayName("성공: 설정 row가 없는 유저는 기본값(켜짐)으로 대상에 포함된다")
		void givenNoStoredSettings_whenFindNotificationTargets_thenIncludeUser() {
			// given
			User user = persistUser(AcademicStatus.ENROLLED);

			// when
			List<User> targets = findTargets(UserNotificationSettingKey.NEWS_CATEGORY_RECRUIT, Set.of());

			// then
			assertThat(targets).containsExactly(user);
		}

		@Test
		@DisplayName("성공: 채널을 끄면 성격 알림이 켜져 있어도 대상에서 제외된다")
		void givenChannelOff_whenFindNotificationTargets_thenExcludeUser() {
			// given
			User user = persistUser(AcademicStatus.ENROLLED);
			entityManager.persist(UserBoardSubscribe.of(user, board, false));

			// when
			List<User> targets = findTargets(UserNotificationSettingKey.NEWS_CATEGORY_RECRUIT, Set.of());

			// then
			assertThat(targets).isEmpty();
		}

		@Test
		@DisplayName("성공: 성격 알림을 끄면 채널이 켜져 있어도 대상에서 제외된다")
		void givenCategoryOff_whenFindNotificationTargets_thenExcludeUser() {
			// given
			User user = persistUser(AcademicStatus.ENROLLED);
			entityManager.persist(UserBoardSubscribe.of(user, board, true));
			persistSetting(user, UserNotificationSettingKey.NEWS_CATEGORY_RECRUIT, false);

			// when
			List<User> targets = findTargets(UserNotificationSettingKey.NEWS_CATEGORY_RECRUIT, Set.of());

			// then
			assertThat(targets).isEmpty();
		}

		@Test
		@DisplayName("성공: 소식 알림 받기를 끄면 채널과 성격이 켜져 있어도 대상에서 제외된다")
		void givenNewsNotificationOff_whenFindNotificationTargets_thenExcludeUser() {
			// given
			User user = persistUser(AcademicStatus.ENROLLED);
			persistSetting(user, UserNotificationSettingKey.NEWS_NOTIFICATION_ENABLED, false);

			// when
			List<User> targets = findTargets(UserNotificationSettingKey.NEWS_CATEGORY_RECRUIT, Set.of());

			// then
			assertThat(targets).isEmpty();
		}

		@Test
		@DisplayName("성공: 성격이 없는 글은 다른 성격 알림을 꺼도 채널 구독만으로 판단한다")
		void givenNoCategoryPost_whenFindNotificationTargets_thenDecideByChannelOnly() {
			// given
			User subscriber = persistUser(AcademicStatus.ENROLLED);
			persistSetting(subscriber, UserNotificationSettingKey.NEWS_CATEGORY_RECRUIT, false);
			User unsubscriber = persistUser(AcademicStatus.ENROLLED);
			entityManager.persist(UserBoardSubscribe.of(unsubscriber, board, false));

			// when
			List<User> targets = findTargets(null, Set.of());

			// then
			assertThat(targets).containsExactly(subscriber);
		}

		@Test
		@DisplayName("성공: 학사 글은 졸업생을 제외하고 재학생에게만 발송된다")
		void givenAcademicPost_whenFindNotificationTargets_thenExcludeGraduatedUser() {
			// given
			User enrolled = persistUser(AcademicStatus.ENROLLED);
			persistUser(AcademicStatus.GRADUATED);

			// when
			List<User> targets = findTargets(UserNotificationSettingKey.NEWS_CATEGORY_ACADEMIC, Set.of());

			// then
			assertThat(targets).containsExactly(enrolled);
		}

		@Test
		@DisplayName("성공: 졸업생 게시판 관리자도 학사 글 알림 대상에서 제외된다")
		void givenGraduatedBoardAdmin_whenFindNotificationTargets_thenExcludeFromAcademicPost() {
			// given
			User graduatedAdmin = persistUser(AcademicStatus.GRADUATED);
			entityManager.flush();

			// when
			List<User> targets = userBoardSubscribeQueryRepository.findNotificationTargets(
				board.getId(), UserNotificationSettingKey.NEWS_CATEGORY_ACADEMIC,
				List.of(AcademicStatus.ENROLLED), Set.of(), Set.of(graduatedAdmin.getId()));

			// then
			assertThat(targets).isEmpty();
		}

		@Test
		@DisplayName("성공: 학사가 아닌 성격의 글은 졸업생에게도 발송된다")
		void givenNonAcademicPost_whenFindNotificationTargets_thenIncludeGraduatedUser() {
			// given
			User graduated = persistUser(AcademicStatus.GRADUATED);

			// when
			List<User> targets = findTargets(UserNotificationSettingKey.NEWS_CATEGORY_RECRUIT, Set.of());

			// then
			assertThat(targets).containsExactly(graduated);
		}
	}

	private List<User> findTargets(UserNotificationSettingKey newsCategoryKey, Set<String> boardAdminIds) {
		entityManager.flush();
		return userBoardSubscribeQueryRepository.findNotificationTargets(
			board.getId(), newsCategoryKey, List.of(), Set.of(), boardAdminIds);
	}

	private User persistUser(AcademicStatus academicStatus) {
		userSequence++;
		User user = User.createGraduate(
			"user" + userSequence + "@test.com",
			"유저" + userSequence,
			"2020" + userSequence,
			2020,
			2024,
			"nickname" + userSequence,
			null,
			"010-0000-000" + userSequence,
			"encoded-password");
		user.setAcademicStatus(academicStatus);
		entityManager.persist(user);
		return user;
	}

	private void persistSetting(User user, UserNotificationSettingKey key, boolean enabled) {
		entityManager.persist(UserNotificationSetting.of(user.getId(), key, enabled));
	}

	@Configuration
	@EnableTransactionManagement
	static class JpaTestConfig {

		@Bean
		DataSource dataSource() {
			return new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.H2)
				.generateUniqueName(true)
				.build();
		}

		@Bean
		LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
			LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
			factory.setDataSource(dataSource);
			factory.setPackagesToScan("net.causw.app.main.domain");
			factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
			Properties properties = new Properties();
			properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
			factory.setJpaProperties(properties);
			return factory;
		}

		@Bean
		PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
			return new JpaTransactionManager(entityManagerFactory);
		}
	}
}
