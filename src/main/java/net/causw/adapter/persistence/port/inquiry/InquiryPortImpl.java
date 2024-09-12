package net.causw.adapter.persistence.port.inquiry;

import net.causw.adapter.persistence.inquiry.Inquiry;
import net.causw.adapter.persistence.port.mapper.DomainModelMapper;
import net.causw.application.spi.InquiryPort;
import net.causw.domain.model.inquiry.InquiryDomainModel;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InquiryPortImpl extends DomainModelMapper implements InquiryPort {
    /*
    private final InquiryRepository inquiryRepository;

    public InquiryPortImpl(InquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    @Override
    public Optional<InquiryDomainModel> findById(String id){
        return this.inquiryRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public InquiryDomainModel create(InquiryDomainModel inquiryDomainModel) {
        return this.entityToDomainModel(this.inquiryRepository.save(Inquiry.from(inquiryDomainModel)));
    }

     */
}
