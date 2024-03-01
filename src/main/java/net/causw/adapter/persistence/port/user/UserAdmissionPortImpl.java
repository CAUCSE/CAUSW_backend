package net.causw.adapter.persistence.port.user;

import net.causw.adapter.persistence.page.PageableFactory;
import net.causw.adapter.persistence.port.mapper.DomainModelMapper;
import net.causw.adapter.persistence.user.UserAdmission;
import net.causw.adapter.persistence.repository.UserAdmissionRepository;
import net.causw.application.spi.UserAdmissionPort;
import net.causw.domain.model.user.UserAdmissionDomainModel;
import net.causw.domain.model.enums.UserState;
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
    public Boolean existsByUserId(String id) {
        return this.userAdmissionRepository.existsByUser_Id(id);
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
    public Page<UserAdmissionDomainModel> findAll(UserState userState, String name, Integer pageNum) {
        Page<UserAdmission> userAdmissions;
        userAdmissions = this.userAdmissionRepository.findAllWithName(userState.getValue(), name, this.pageableFactory.create(pageNum));
        return userAdmissions.map(this::entityToDomainModel);
    }

    @Override
    public UserAdmissionDomainModel create(UserAdmissionDomainModel userAdmissionDomainModel) {
        return this.entityToDomainModel(
                this.userAdmissionRepository.save(UserAdmission.from(userAdmissionDomainModel))
        );
    }

    @Override
    public void delete(UserAdmissionDomainModel userAdmissionDomainModel) {
        this.userAdmissionRepository.delete(UserAdmission.from(userAdmissionDomainModel));
    }
}
