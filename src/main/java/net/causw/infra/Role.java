package net.causw.infra;

public enum Role {
    ADMIN("admin"),
    PRESIDENT("president"),
    COUNCIL("council"),
    LEADER_1("leader_1"),
    LEADER_2("leader_2"),
    LEADER_3("leader_3"),
    LEADER_4("leader_4"),
    LEADER_CIRCLE("leader_circle"),
    LEADER_ALUMNI("leader_alumni"),
    COMMON("common"),
    PROFESSOR("professor");

    private String value;

    Role(String value) {
        this.value = value;
    }
}
