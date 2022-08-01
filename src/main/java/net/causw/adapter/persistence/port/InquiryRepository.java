package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, String> {

}