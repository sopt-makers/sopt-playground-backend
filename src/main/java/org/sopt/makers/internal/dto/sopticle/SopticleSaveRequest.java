package org.sopt.makers.internal.dto.sopticle;

import java.util.List;

public record SopticleSaveRequest(
        String link,
        List<Long> writerIds
) {
}
