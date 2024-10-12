package org.sopt.makers.internal.member.util.coffeechat;

import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatTopicType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class CoffeeChatTopicTypeConverter implements AttributeConverter<List<CoffeeChatTopicType>, String> {

	@Override
	public String convertToDatabaseColumn(List<CoffeeChatTopicType> topicTypes) {
		if (topicTypes == null || topicTypes.isEmpty()) {
			return "";
		}
		return topicTypes.stream()
				.map(CoffeeChatTopicType::name)
				.collect(Collectors.joining(","));
	}

	@Override
	public List<CoffeeChatTopicType> convertToEntityAttribute(String rawData) {
		if (rawData == null || rawData.isEmpty()) {
			return List.of();
		}
		return Arrays.stream(rawData.split(","))
				.map(CoffeeChatTopicType::valueOf)
				.toList();
	}
}
