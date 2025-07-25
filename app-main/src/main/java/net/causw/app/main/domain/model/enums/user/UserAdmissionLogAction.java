package net.causw.app.main.domain.model.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserAdmissionLogAction {
    ACCEPT("accept"),
    REJECT("reject");

    private final String value;

}
