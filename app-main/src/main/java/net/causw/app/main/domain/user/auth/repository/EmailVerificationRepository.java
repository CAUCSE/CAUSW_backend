package net.causw.app.main.domain.user.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.causw.app.main.domain.user.auth.entity.EmailVerification;
import net.causw.app.main.domain.user.auth.entity.EmailVerification.VerificationStatus;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, String> {

	@Query("""
		SELECT ev FROM EmailVerification ev
		WHERE ev.email = :email
		  AND ev.status = :status
		ORDER BY ev.createdAt DESC
		LIMIT 1
		""")
	Optional<EmailVerification> findLatestByEmailAndStatus(
		@Param("email") String email,
		@Param("status") VerificationStatus status);

	@Query("""
		SELECT ev FROM EmailVerification ev
		WHERE ev.email = :email
		  AND ev.status = 'VERIFIED'
		  AND ev.verificationCode = :verificationCode
		ORDER BY ev.createdAt DESC
		LIMIT 1
		""")
	Optional<EmailVerification> findVerifiedByEmailAndCode(@Param("email") String email,
		@Param("verificationCode") String verificationCode);

	@Query("""
		SELECT ev FROM EmailVerification ev
		WHERE ev.email = :email
		ORDER BY ev.createdAt DESC
		LIMIT 1
		""")
	Optional<EmailVerification> findLatestByEmail(@Param("email") String email);
}
