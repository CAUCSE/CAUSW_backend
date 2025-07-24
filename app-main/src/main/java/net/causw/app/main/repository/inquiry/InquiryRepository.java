package net.causw.app.main.repository.inquiry;

import net.causw.app.main.domain.model.entity.inquiry.Inquiry;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, String> {

}