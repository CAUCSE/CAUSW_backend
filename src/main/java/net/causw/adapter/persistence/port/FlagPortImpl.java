package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Flag;
import net.causw.adapter.persistence.FlagRepository;
import net.causw.application.spi.FlagPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FlagPortImpl implements FlagPort {
    private final FlagRepository flagRepository;

    public FlagPortImpl(FlagRepository flagRepository) {
        this.flagRepository = flagRepository;
    }

    @Override
    public Optional<Boolean> findByKey(String key) {
        return this.flagRepository.findByKey(key).map(Flag::getValue);
    }

    @Override
    public Boolean create(String key, Boolean value) {
        return this.flagRepository.save(Flag.of(key, value)).getValue();
    }

    @Override
    public Optional<Boolean> update(String key, Boolean value) {
        return this.flagRepository.findByKey(key).map(
                flag -> {
                    flag.setValue(value);

                    return this.flagRepository.save(flag).getValue();
                }
        );
    }
}
