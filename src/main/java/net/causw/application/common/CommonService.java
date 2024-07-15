package net.causw.application.common;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.flag.Flag;
import net.causw.adapter.persistence.repository.FlagRepository;
import net.causw.adapter.persistence.repository.TextFieldRepository;
import net.causw.adapter.persistence.textfield.TextField;
import net.causw.application.spi.FlagPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommonService {
    private final TextFieldRepository textFieldRepository;
    private final FlagRepository flagRepository;


    @Transactional
    public String createTextField(String key, String value) {
        return textFieldRepository.save(TextField.of(key, value)).getValue();
    }

    @Transactional
    public Optional<String> updateTextField(String key, String value) {
        return textFieldRepository.findByKey(key).map(
                flag -> {
                    flag.setValue(value);

                    return this.textFieldRepository.save(flag).getValue();
                }
        );
    }

    public Boolean createFlag(String loginUserId, String key, Boolean value) {

        return flagRepository.save(Flag.of(key, value)).getValue();
    }

    public Boolean updateFlag(String loginUserId , String key, Boolean value) {
        return flagRepository.findByKey(key).map(
                flag -> {
                    flag.setValue(value);

                    return this.flagRepository.save(flag).getValue();
                }
        ).orElse(false);
    }
    public Optional<String> findByKeyInTextField(String key) {
        return textFieldRepository.findByKey(key).map(TextField::getValue);
    }

    public Optional<Boolean> findByKeyInFlag(String key) {
        return flagRepository.findByKey(key).map(Flag::getValue);
    }

}
