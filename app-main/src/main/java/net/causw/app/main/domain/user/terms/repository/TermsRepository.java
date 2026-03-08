package net.causw.app.main.domain.user.terms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.terms.entity.Terms;

@Repository
public interface TermsRepository extends JpaRepository<Terms, String> {

	@Query("SELECT t FROM Terms t WHERE t.version = (SELECT MAX(t2.version) FROM Terms t2 WHERE t2.type = t.type)")
	List<Terms> findLatestVersionPerType();
}
