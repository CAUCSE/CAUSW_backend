package net.causw.domain.model;

public enum UserAdmissionLogAction {
    ACCEPT("accept"),
    REJECT("reject");

    private String value;

    UserAdmissionLogAction(String value) {
        this.value = value;
    }
}
