package net.causw.domain.validation;

import java.util.LinkedList;
import java.util.List;

public class ValidatorBucket {
    private final List<AbstractValidator> validatorList;

    private ValidatorBucket() {
        this.validatorList = new LinkedList<>();
    }

    public static ValidatorBucket of() {
        return new ValidatorBucket();
    }

    public ValidatorBucket consistOf(AbstractValidator abstractValidator) {
        this.validatorList.add(abstractValidator);
        return this;
    }

    public void validate() {
        this.validatorList.forEach(AbstractValidator::validate);
    }
}
