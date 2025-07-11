package org.sopt.makers.internal.common.util;

import lombok.val;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InfiniteScrollUtil {
    public Integer checkLimitForPagination(Integer limit) {
        val isLimitEmpty = (limit == null || limit == 0);
        return isLimitEmpty ? null : limit + 1;
    }

    public <T extends Record> Boolean checkHasNextElement(Integer limit, List<T> elementList) {
        val hasNextElement = ((limit != null && limit != 0) && elementList.size() > limit);

        elementList = new ArrayList<>(elementList);
        if (hasNextElement) {
            elementList.remove(elementList.size() - 1);
        }
        return hasNextElement;
    }
}