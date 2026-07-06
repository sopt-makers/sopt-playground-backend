package org.sopt.makers.internal.common.util;

import lombok.val;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InfiniteScrollUtil {
    public Integer checkLimitForPagination(Integer limit) {
        val isLimitEmpty = (limit == null || limit <= 0);
        return isLimitEmpty ? null : limit + 1;
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