package net.causw.app.main.domain.user.terms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.terms.entity.Terms;

@Repository
public interface TermsRepository extends JpaRepository<Terms, String> {

	Optional<Terms> findTopByOrderByCreatedAtDesc();
}
