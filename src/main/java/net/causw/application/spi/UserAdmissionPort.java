package net.causw.application.spi;

import net.causw.domain.model.UserAdmissionDomainModel;
import net.causw.domain.model.UserState;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface UserAdmissionPort {
    Optional<UserAdmissionDomainModel> findById(String id);

    Page<UserAdmissionDomainModel> findAll(UserState userState, Integer pageNum);

    UserAdmissionDomainModel create(UserAdmissionDomainModel userAdmissionDomainModel);
}
