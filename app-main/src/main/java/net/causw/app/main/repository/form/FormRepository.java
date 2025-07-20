package net.causw.app.main.repository.form;

import net.causw.app.main.domain.model.entity.circle.Circle;
import net.causw.app.main.domain.model.entity.form.Form;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormRepository extends JpaRepository<Form, String> {
    @NotNull Optional<Form> findByIdAndIsDeleted(@NotNull String id, Boolean isDeleted);

    List<Form> findAllByCircleAndIsDeletedAndIsClosed(Circle circle, Boolean isDeleted, Boolean isClosed);

    List<Form> findAllByCircleAndIsDeleted(Circle circle, Boolean isDeleted);

    Page<Form> findAllByCircle(Circle circle, Pageable pageable);

    List<Form> findAllByCircle(Circle circle);
}
