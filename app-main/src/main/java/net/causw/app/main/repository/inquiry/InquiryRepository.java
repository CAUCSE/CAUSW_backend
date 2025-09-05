package net.causw.app.main.repository.inquiry;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.model.entity.inquiry.Inquiry;

public interface InquiryRepository extends JpaRepository<Inquiry, String> {

}