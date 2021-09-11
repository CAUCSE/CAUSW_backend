package net.causw.domain.model;

public enum CircleMemberStatus {
    AWAIT("await"),
    MEMBER("member"),
    LEAVE("leave"),
    DROP("drop"),
    REJECT("reject");

    private String value;

    CircleMemberStatus(String value) {
        this.value = value;
    }
}