package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.PageableFactory;
import net.causw.adapter.persistence.UserAdmission;
import net.causw.adapter.persistence.UserAdmissionRepository;
import net.causw.application.spi.UserAdmissionPort;
import net.causw.domain.model.UserAdmissionDomainModel;
import net.causw.domain.model.UserState;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserAdmissionPortImpl extends DomainModelMapper implements UserAdmissionPort {
    private final UserAdmissionRepository userAdmissionRepository;
    private final PageableFactory pageableFactory;

    public UserAdmissionPortImpl(
            UserAdmissionRepository userAdmissionRepository,
            PageableFactory pageableFactory
    ) {
        this.userAdmissionRepository = userAdmissionRepository;
        this.pageableFactory = pageableFactory;
    }

    @Override
    public Optional<UserAdmissionDomainModel> findById(String id) {
        return this.userAdmissionRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public Optional<UserAdmissionDomainModel> findByUserId(String id) {
        return this.userAdmissionRepository.findByUser_Id(id).map(this::entityToDomainModel);
    }

    @Override
    public Page<UserAdmissionDomainModel> findAll(UserState userState, Integer pageNum) {
        return this.userAdmissionRepository.findAll(userState.toString(), this.pageableFactory.create(pageNum))
                .map(this::entityToDomainModel);
    }

    @Override
    public UserAdmissionDomainModel create(UserAdmissionDomainModel userAdmissionDomainModel) {
        return this.entityToDomainModel(
                this.userAdmissionRepository.save(UserAdmission.from(userAdmissionDomainModel))
        );
    }
}
