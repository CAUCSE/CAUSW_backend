package net.causw.app.main.dto.chat.socket;

import net.causw.app.main.domain.model.enums.socket.SocketPayloadType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SocketPayload<T> {
	private SocketPayloadType type;
	private T data;

	public static <T> SocketPayload<T> of(SocketPayloadType type, T data) {
		return new SocketPayload<T>(type, data);
	}
}
