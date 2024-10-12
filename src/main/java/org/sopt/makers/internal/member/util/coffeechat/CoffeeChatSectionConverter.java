package org.sopt.makers.internal.member.util.coffeechat;

import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatSection;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChatTopicType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class CoffeeChatSectionConverter implements AttributeConverter<List<CoffeeChatSection>, String> {

	@Override
	public String convertToDatabaseColumn(List<CoffeeChatSection> sections) {
		if (sections == null || sections.isEmpty()) {
			return "";
		}
		return sections.stream()
				.map(CoffeeChatSection::name)
				.collect(Collectors.joining(","));
	}

	@Override
	public List<CoffeeChatSection> convertToEntityAttribute(String rawData) {
		if (rawData == null || rawData.isEmpty()) {
			return List.of();
		}
		return Arrays.stream(rawData.split(","))
				.map(CoffeeChatSection::valueOf)
				.toList();
	}
}
