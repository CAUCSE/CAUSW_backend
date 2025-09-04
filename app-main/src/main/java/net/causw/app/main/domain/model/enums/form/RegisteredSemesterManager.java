package net.causw.app.main.domain.model.enums.form;

import java.util.EnumSet;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RegisteredSemesterManager {

	private EnumSet<RegisteredSemester> registeredSemesterEnumSet = EnumSet.noneOf(RegisteredSemester.class);

	public String serialize() {
		if (this.registeredSemesterEnumSet.isEmpty()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (RegisteredSemester registeredSemester : registeredSemesterEnumSet) {
			sb.append(registeredSemester.name());
			sb.append(",");
		}
		return sb.toString();
	}

	public void deserialize(String registeredSemesterString) {
		if (registeredSemesterString.isBlank() || registeredSemesterString.isEmpty()) {
			this.registeredSemesterEnumSet.clear();
			return;
		}
		String[] registeredSemesterArray = registeredSemesterString.split(",");
		for (String registeredSemester : registeredSemesterArray) {
			this.registeredSemesterEnumSet.add(RegisteredSemester.valueOf(registeredSemester));
		}
	}

	public static RegisteredSemesterManager fromEnumList(
		List<RegisteredSemester> registeredSemesterList
	) {
		if (registeredSemesterList.isEmpty()) {
			return new RegisteredSemesterManager();
		}
		return new RegisteredSemesterManager(EnumSet.copyOf(registeredSemesterList));
	}

	public static RegisteredSemesterManager fromString(
		String registeredSemesterString
	) {
		if (registeredSemesterString == null) {
			return new RegisteredSemesterManager();
		}
		if (registeredSemesterString.isBlank() || registeredSemesterString.isEmpty()) {
			return new RegisteredSemesterManager();
		}
		RegisteredSemesterManager registeredSemesterManager = new RegisteredSemesterManager();
		registeredSemesterManager.deserialize(registeredSemesterString);
		return registeredSemesterManager;
	}

}
