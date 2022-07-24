package net.causw.adapter.persistence.port;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.TextField;
import net.causw.adapter.persistence.TextFieldRepository;
import net.causw.application.spi.TextFieldPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TextFieldPortImpl implements TextFieldPort {
    private final TextFieldRepository textFieldRepository;

    @Override
    public Optional<String> findByKey(String key) {
        return this.textFieldRepository.findByKey(key).map(TextField::getValue);
    }

    @Override
    public String create(String key, String value) {
        return this.textFieldRepository.save(TextField.of(key, value)).getValue();
    }

    @Override
    public Optional<String> update(String key, String value) {
        return this.textFieldRepository.findByKey(key).map(
                flag -> {
                    flag.setValue(value);

                    return this.textFieldRepository.save(flag).getValue();
                }
        );
    }
}
