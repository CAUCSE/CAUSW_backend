package net.causw.domain.model.enums;

public enum UserAdmissionLogAction {
    ACCEPT("accept"),
    REJECT("reject");

    private String value;

    UserAdmissionLogAction(String value) {
        this.value = value;
    }
}
