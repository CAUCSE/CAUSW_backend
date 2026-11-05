package net.causw.app.main.domain.campus.event.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import net.causw.app.main.shared.storage.StorageClient;

@SpringJUnitConfig(EventImageDeletionEventListenerTest.Config.class)
class EventImageDeletionEventListenerTest {

	private static final String FILE_KEY = "event/old.png";

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private StorageClient storageClient;

	private TransactionTemplate transactionTemplate;

	@BeforeEach
	void setUp() {
		reset(storageClient);
		transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Test
	@DisplayName("트랜잭션이 끝나기 전에는 파일을 삭제하지 않고 커밋 후에 삭제한다")
	void givenCommit_whenPublish_thenDeleteOnlyAfterCommit() {
		transactionTemplate.executeWithoutResult(status -> {
			applicationEventPublisher.publishEvent(new EventImageDeletionRequestedEvent(FILE_KEY));
			verify(storageClient, never()).delete(FILE_KEY);
		});

		verify(storageClient).delete(FILE_KEY);
	}

	@Test
	@DisplayName("트랜잭션이 롤백되면 파일을 삭제하지 않는다")
	void givenRollback_whenPublish_thenKeepFile() {
		transactionTemplate.executeWithoutResult(status -> {
			applicationEventPublisher.publishEvent(new EventImageDeletionRequestedEvent(FILE_KEY));
			status.setRollbackOnly();
		});

		verify(storageClient, never()).delete(FILE_KEY);
	}

	@Test
	@DisplayName("파일 삭제에 실패해도 예외를 밖으로 전파하지 않는다")
	void givenStorageFailure_whenCommit_thenNoException() {
		willThrow(new RuntimeException("storage error")).given(storageClient).delete(FILE_KEY);

		assertThatCode(() -> transactionTemplate.executeWithoutResult(
			status -> applicationEventPublisher.publishEvent(new EventImageDeletionRequestedEvent(FILE_KEY))))
			.doesNotThrowAnyException();

		verify(storageClient).delete(FILE_KEY);
	}

	@Configuration
	@EnableTransactionManagement
	@Import(EventImageDeletionEventListener.class)
	static class Config {

		@Bean
		StorageClient storageClient() {
			return mock(StorageClient.class);
		}

		@Bean
		PlatformTransactionManager transactionManager() {
			return new AbstractPlatformTransactionManager() {
				@Override
				protected Object doGetTransaction() {
					return new Object();
				}

				@Override
				protected void doBegin(Object transaction, TransactionDefinition definition) {}

				@Override
				protected void doCommit(DefaultTransactionStatus status) {}

				@Override
				protected void doRollback(DefaultTransactionStatus status) {}
			};
		}
	}
}
