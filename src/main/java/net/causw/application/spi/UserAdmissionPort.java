package net.causw.application.spi;

import net.causw.domain.model.UserAdmissionDomainModel;
import net.causw.domain.model.UserState;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface UserAdmissionPort {
    Boolean existsByUserId(String id);

    Optional<UserAdmissionDomainModel> findById(String id);

    Optional<UserAdmissionDomainModel> findByUserId(String id);

    Page<UserAdmissionDomainModel> findAll(UserState userState, Integer pageNum);

    UserAdmissionDomainModel create(UserAdmissionDomainModel userAdmissionDomainModel);

    void delete(UserAdmissionDomainModel userAdmissionDomainModel);
}
