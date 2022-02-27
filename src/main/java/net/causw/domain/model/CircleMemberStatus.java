package net.causw.domain.model;

public enum CircleMemberStatus {
    AWAIT("AWAIT"),
    MEMBER("MEMBER"),
    LEAVE("LEAVE"),
    DROP("DROP"),
    REJECT("REJECT");

    private final String value;

    CircleMemberStatus(String value) {
        this.value = value;
    }
}