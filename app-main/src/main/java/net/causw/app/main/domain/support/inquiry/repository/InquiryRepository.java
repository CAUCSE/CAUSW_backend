package net.causw.app.main.domain.support.inquiry.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.support.inquiry.entity.Inquiry;

public interface InquiryRepository extends JpaRepository<Inquiry, String> {

}