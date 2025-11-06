package net.causw.app.main.domain.community.repository.form;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.entity.form.Form;
import net.causw.app.main.domain.moving.model.entity.circle.Circle;

@Repository
public interface FormRepository extends JpaRepository<Form, String> {
	@NotNull
	Optional<Form> findByIdAndIsDeleted(@NotNull String id, Boolean isDeleted);

	List<Form> findAllByCircleAndIsDeletedAndIsClosed(Circle circle, Boolean isDeleted, Boolean isClosed);

	List<Form> findAllByCircleAndIsDeleted(Circle circle, Boolean isDeleted);

	Page<Form> findAllByCircle(Circle circle, Pageable pageable);

	List<Form> findAllByCircle(Circle circle);
}
