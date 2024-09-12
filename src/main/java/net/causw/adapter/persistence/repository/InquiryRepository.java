package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.inquiry.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, String> {

}