package net.causw.adapter.persistence.repository.form;

import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.form.Form;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormRepository extends JpaRepository<Form, String> {
    @NotNull Optional<Form> findById(@NotNull String id);

    Optional<Form> findByCircle(Circle circle);

    List<Form> findAllByCircleAndIsDeletedAndIsClosed(Circle circle, Boolean isDeleted, Boolean isClosed);

    List<Form> findAllByCircleAndIsDeleted(Circle circle, Boolean isDeleted);

    Page<Form> findAllByCircle(Circle circle, Pageable pageable);

    List<Form> findAllByCircle(Circle circle);
}
