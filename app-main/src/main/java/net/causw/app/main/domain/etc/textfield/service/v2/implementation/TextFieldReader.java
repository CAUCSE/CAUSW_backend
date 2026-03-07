package net.causw.app.main.domain.etc.textfield.service.v2.implementation;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.etc.textfield.entity.TextField;
import net.causw.app.main.domain.etc.textfield.repository.TextFieldRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TextFieldReader {

	private final TextFieldRepository textFieldRepository;

	public Optional<String> findValueByKey(String key) {
		return textFieldRepository.findByKey(key).map(TextField::getValue);
	}
}
