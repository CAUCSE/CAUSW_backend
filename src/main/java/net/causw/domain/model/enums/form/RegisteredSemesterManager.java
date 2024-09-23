package net.causw.domain.model.enums.form;

import lombok.Getter;

import java.util.EnumSet;
import java.util.List;

@Getter
public class RegisteredSemesterManager {

    private EnumSet<RegisteredSemester> registeredSemesterEnumSet = EnumSet.noneOf(RegisteredSemester.class);

    public void enableRegisteredSemester(RegisteredSemester registeredSemester) {
        registeredSemesterEnumSet.add(registeredSemester);
    }

    public void disableRegisteredSemester(RegisteredSemester registeredSemester) {
        registeredSemesterEnumSet.remove(registeredSemester);
    }

    public void enableAllRegisteredSemester() {
        registeredSemesterEnumSet = EnumSet.allOf(RegisteredSemester.class);
    }

    public void disableAllRegisteredSemester() {
        registeredSemesterEnumSet.clear();
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for (RegisteredSemester registeredSemester : registeredSemesterEnumSet) {
            sb.append(registeredSemester.name());
            sb.append(",");
        }
        return sb.toString();
    }

    public void deserialize(String registeredSemesterString) {
        String[] registeredSemesterArray = registeredSemesterString.split(",");
        for (String registeredSemester : registeredSemesterArray) {
            this.registeredSemesterEnumSet.add(RegisteredSemester.valueOf(registeredSemester));
        }
    }

    public static RegisteredSemesterManager from (
            List<RegisteredSemester> registeredSemesterList
    ) {
        RegisteredSemesterManager registeredSemesterManager = new RegisteredSemesterManager();
        for (RegisteredSemester registeredSemester : registeredSemesterList) {
            registeredSemesterManager.enableRegisteredSemester(registeredSemester);
        }
        return registeredSemesterManager;
    }

}
