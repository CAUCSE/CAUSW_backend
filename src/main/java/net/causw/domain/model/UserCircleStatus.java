package net.causw.domain.model;

public enum UserCircleStatus {
    AWAIT("await"),
    MEMBER("member"),
    LEAVE("leave"),
    DROP("drop");

    private String value;

    UserCircleStatus(String value) {
        this.value = value;
    }
}