package net.causw.adapter.persistence.port.inquiry;

import net.causw.adapter.persistence.inquiry.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, String> {

}