package org.sopt.makers.internal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MemberSaveRequest (
        String name,
        @JsonProperty("auth_user_id")
        Long authId,
        Integer generation
) {}
