package net.causw.domain.model.enums;


public enum BoardApplyStatus {
    AWAIT("AWAIT"),
    ACCEPTED("ACCEPTED"),
    REJECT("REJECT");

    private final String value;

    BoardApplyStatus(String value) {
        this.value = value;
    }
}
