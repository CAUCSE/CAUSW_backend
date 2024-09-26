package net.causw.domain.model.enums.circle;

import lombok.Getter;

@Getter
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