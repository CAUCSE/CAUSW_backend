package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.form.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormRepository extends JpaRepository<Form, String> {
    Optional<Form> findById(String id);
}
