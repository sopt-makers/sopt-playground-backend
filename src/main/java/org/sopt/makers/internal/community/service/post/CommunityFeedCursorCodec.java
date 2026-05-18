package org.sopt.makers.internal.community.service.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.dto.CommunityFeedCursor;
import org.sopt.makers.internal.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityFeedCursorCodec {

	private final ObjectMapper objectMapper;

	public CommunityFeedCursor decodeOrInitial(String cursor) {
		if (cursor == null || cursor.isBlank()) {
			return CommunityFeedCursor.initial(LocalDateTime.now());
		}

		try {
			String json = new String(
				Base64.getUrlDecoder().decode(cursor),
				StandardCharsets.UTF_8
			);

			return objectMapper.readValue(json, CommunityFeedCursor.class);
		} catch (Exception e) {
			throw new BadRequestException("유효하지 않은 cursor 값입니다.");
		}
	}

	public String encode(CommunityFeedCursor cursor) {
		try {
			String json = objectMapper.writeValueAsString(cursor);

			return Base64.getUrlEncoder()
				.withoutPadding()
				.encodeToString(json.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new IllegalStateException("cursor 생성에 실패했습니다.", e);
		}
	}
}