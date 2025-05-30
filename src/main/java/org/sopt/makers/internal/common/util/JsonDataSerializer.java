package org.sopt.makers.internal.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonDataSerializer {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static Object serialize(String rawData) {
		Object jsonObject;

		try {
			jsonObject = objectMapper.readValue(rawData, Object.class);
		} catch (JsonProcessingException e) {
			log.error("Error parsing JSON data ::", e);
			return "";
		}

		if (jsonObject instanceof String) {
			String str = (String) jsonObject;
			if (str.matches("-?\\d+(\\.\\d+)?")) {
				return Double.parseDouble(str);
			}
			return str;
		}

		return jsonObject;
	}
}
