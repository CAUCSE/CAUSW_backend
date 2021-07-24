package net.causw.domain.model;

public enum LockerLogAction {
    ENABLE("활성화"),
    DISABLE("비활성화"),
    REGISTER("신청"),
    RETURN("반납");

    private String value;

    LockerLogAction(String value) {
        this.value = value;
    }
}
