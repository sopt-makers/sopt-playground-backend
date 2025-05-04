package org.sopt.makers.internal.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record InternalProfileListRequest(

        @Schema(required = true)
        List<Long> memberIds
) {
}
