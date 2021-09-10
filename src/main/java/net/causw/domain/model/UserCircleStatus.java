package net.causw.domain.model;

public enum UserCircleStatus {
    AWAIT("await"),
    MEMBER("member"),
    LEAVE("leave"),
    DROP("drop"),
    REJECT("reject");

    private String value;

    UserCircleStatus(String value) {
        this.value = value;
    }
}