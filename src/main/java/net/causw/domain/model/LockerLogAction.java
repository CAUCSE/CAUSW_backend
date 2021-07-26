package net.causw.domain.model;

public enum LockerLogAction {
    ENABLE("enable"),
    DISABLE("disable"),
    REGISTER("register"),
    RETURN("return");

    private String value;

    LockerLogAction(String value) {
        this.value = value;
    }
}
