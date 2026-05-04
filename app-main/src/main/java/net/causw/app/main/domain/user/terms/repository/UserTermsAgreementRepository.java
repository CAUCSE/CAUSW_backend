package net.causw.app.main.domain.user.terms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.terms.entity.UserTermsAgreement;

@Repository
public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreement, String> {

	List<UserTermsAgreement> findByUser(User user);

	List<UserTermsAgreement> findByUserAndTerms_IdIn(User user, List<String> termsIds);

	/**
	 * 타입별 최신 버전이면서 필수인 약관 각각에 대해 사용자 동의가 있는지 판별합니다.
	 * 필수 최신 약관이 하나도 없으면 {@code true}입니다.
	 */
	@Query("""
		SELECT CASE WHEN NOT EXISTS (
			SELECT r FROM Terms r
			WHERE r.isRequired = true
			AND r.version = (SELECT MAX(r2.version) FROM Terms r2 WHERE r2.type = r.type)
			AND NOT EXISTS (
				SELECT uta FROM UserTermsAgreement uta
				WHERE uta.user = :user AND uta.terms = r
			)
		) THEN true ELSE false END
		FROM User u WHERE u = :user
		""")
	boolean hasAgreedToAllRequiredLatestTerms(@Param("user") User user);
}
