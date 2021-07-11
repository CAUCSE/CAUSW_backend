package net.causw.infra;

public enum LockerType {
        APPLY("신청"),
        RETURN("반납");

        private String value;

        LockerType(String value) {
            this.value = value;
        }
}
