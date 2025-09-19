package org.sopt.makers.internal.external.platform;

import java.util.List;

public record GetPlatformUserBatchRequest(
        List<Long> userIds
) {}