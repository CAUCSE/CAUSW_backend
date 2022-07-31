package net.causw.application.spi;

import net.causw.domain.model.InquiryDomainModel;
import net.causw.domain.model.UserDomainModel;

import java.util.Optional;

public interface InquiryPort {
    Optional<InquiryDomainModel> findById(String id);
    InquiryDomainModel create(InquiryDomainModel inquiryDomainModel);

}
