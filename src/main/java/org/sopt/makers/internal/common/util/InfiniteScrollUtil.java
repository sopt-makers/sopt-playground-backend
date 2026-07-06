package org.sopt.makers.internal.common.util;

import org.sopt.makers.internal.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InfiniteScrollUtil {
    public Integer checkLimitForPagination(Integer limit) {
        if (limit == null || limit == 0) {
            return null;
        }

        if (limit < 0) {
            throw new BadRequestException("limit은 0 이상이어야 합니다.");
        }

        return limit + 1;
    }

    public <T> Boolean checkHasNextElement(Integer limit, List<T> elementList) {
        return (limit != null && limit > 0) && elementList.size() > limit;
    }

    public <T> List<T> removeNextElementIfExist(Integer limit, List<T> elementList) {
        if (!checkHasNextElement(limit, elementList)) {
            return elementList;
        }

        return new ArrayList<>(elementList.subList(0, limit));
    }
}