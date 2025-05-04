package org.sopt.makers.internal.deprecated.soulmate.domain;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class SoulmateStateConverter implements AttributeConverter<SoulmateState, String>{
    @Override
    public String convertToDatabaseColumn(SoulmateState state) {
        return state.getState();
    }

    @Override
    public SoulmateState convertToEntityAttribute(String stateString) {
        return SoulmateState.of(stateString);
    }
}
