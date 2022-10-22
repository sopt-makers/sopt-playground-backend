package org.sopt.makers.internal.dto.member;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MemberSaveRequest (
        String name,
        @JsonProperty("auth_user_id")
        String authId,
        Integer generation
) {}
