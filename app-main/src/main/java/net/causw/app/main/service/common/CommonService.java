package net.causw.app.main.service.common;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.model.entity.flag.Flag;
import net.causw.app.main.repository.flag.FlagRepository;
import net.causw.app.main.repository.textField.TextFieldRepository;
import net.causw.app.main.domain.model.entity.textfield.TextField;
import net.causw.app.main.infrastructure.aop.annotation.MeasureTime;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Optional;

@MeasureTime
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

    public Boolean createFlag(String key, Boolean value) {

        return flagRepository.save(Flag.of(key, value)).getValue();
    }

    public Boolean updateFlag(String key, Boolean value) {
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
