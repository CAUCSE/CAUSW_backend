package net.causw.adapter.persistence.repository.form;

import net.causw.adapter.persistence.form.Form;
import net.causw.adapter.persistence.form.Reply;
import net.causw.adapter.persistence.user.User;
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

}
