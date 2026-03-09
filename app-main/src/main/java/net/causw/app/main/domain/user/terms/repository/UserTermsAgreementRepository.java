package net.causw.app.main.domain.user.terms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.terms.entity.Terms;
import net.causw.app.main.domain.user.terms.entity.UserTermsAgreement;

@Repository
public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreement, String> {

	List<UserTermsAgreement> findByUser(User user);

	boolean existsByUserAndTerms(User user, Terms terms);
}
