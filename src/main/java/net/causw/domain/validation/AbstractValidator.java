package net.causw.domain.validation;

public abstract class AbstractValidator {

    protected AbstractValidator next;

    public AbstractValidator linkWith(AbstractValidator validator) {
        this.next = validator;
        return this;
    }

    protected boolean hasNext() {
        return this.next != null;
    }

    public abstract void validate();
}
