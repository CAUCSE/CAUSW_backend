package net.causw.app.main.repository.form;

import net.causw.app.main.domain.model.entity.form.Form;
import net.causw.app.main.domain.model.entity.form.Reply;
import net.causw.app.main.domain.model.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

    Boolean existsByFormAndUser(Form form, User writer);

    Page<Reply> findAllByForm(Form form, Pageable pageable);

    List<Reply> findAllByForm(Form form);

    List<Reply> findByFormAndUser(Form form, User user);

}
