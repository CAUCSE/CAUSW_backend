package net.causw.app.main.domain.model.enums.ceremony;

public enum CeremonyCategory {
    MARRIAGE("결혼식"),
    FUNERAL("장례식"),
    GRADUATION("졸업식"),
    ETC("기타");

    private final String label;

    CeremonyCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
