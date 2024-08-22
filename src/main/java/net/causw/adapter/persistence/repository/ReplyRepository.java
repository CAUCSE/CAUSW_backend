package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.form.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReplyRepository  extends JpaRepository<Reply, String> {
    Optional<Reply> findById(String id);

}