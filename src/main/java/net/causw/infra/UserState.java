package net.causw.infra;

import lombok.Getter;

@Getter
public enum UserState {
    WAIT("wait"),
    ACTIVE("active"),
    INACTIVE("inactive"),
    BLOCKED("blocked");

    private String value;

    UserState(String value) {
        this.value = value;
    }
}
