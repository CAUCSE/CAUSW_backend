package net.causw.app.main.domain.model.entity.form;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReplySelectedOptionManager {

	private List<Integer> selectedOptionList = new ArrayList<>();

	public static ReplySelectedOptionManager fromIntegerList(
		List<Integer> selectedOptionList
	) {
		if (selectedOptionList == null || selectedOptionList.isEmpty()) {
			return new ReplySelectedOptionManager();
		}
		selectedOptionList.sort(Integer::compareTo);
		return new ReplySelectedOptionManager(selectedOptionList);
	}

	public static ReplySelectedOptionManager fromString(
		String selectedOptionString
	) {
		if (selectedOptionString == null || selectedOptionString.isBlank() || selectedOptionString.isEmpty()) {
			return new ReplySelectedOptionManager();
		}
		ReplySelectedOptionManager replySelectedOptionManager = new ReplySelectedOptionManager();
		replySelectedOptionManager.deserialize(selectedOptionString);
		return replySelectedOptionManager;
	}

	public String serialize() {
		if (this.selectedOptionList == null || this.selectedOptionList.isEmpty()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (Integer selectedOption : this.selectedOptionList) {
			sb.append(selectedOption);
			sb.append(",");
		}
		return sb.toString();
	}

	public void deserialize(String selectedOptionString) {
		if (selectedOptionString == null || selectedOptionString.isBlank() || selectedOptionString.isEmpty()) {
			this.selectedOptionList.clear();
			return;
		}
		String[] selectedOptionArray = selectedOptionString.split(",");
		for (String selectedOption : selectedOptionArray) {
			this.selectedOptionList.add(Integer.parseInt(selectedOption));
		}
	}

}
