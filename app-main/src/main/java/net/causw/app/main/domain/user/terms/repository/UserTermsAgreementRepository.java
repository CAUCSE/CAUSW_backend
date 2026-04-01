package net.causw.app.main.domain.user.terms.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.terms.entity.UserTermsAgreement;

@Repository
public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreement, String> {

	List<UserTermsAgreement> findByUser(User user);

	List<UserTermsAgreement> findByUserAndTerms_IdIn(User user, List<String> termsIds);

	long countByUserAndTerms_IdIn(User user, Set<String> termsIds);
}
