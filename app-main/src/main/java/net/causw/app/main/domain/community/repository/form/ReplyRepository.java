package net.causw.app.main.domain.community.repository.form;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.entity.form.Form;
import net.causw.app.main.domain.community.entity.form.Reply;
import net.causw.app.main.domain.user.entity.user.User;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

	Boolean existsByFormAndUser(Form form, User writer);

	Page<Reply> findAllByForm(Form form, Pageable pageable);

	List<Reply> findAllByForm(Form form);

	List<Reply> findByFormAndUser(Form form, User user);

}
