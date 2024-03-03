package org.sopt.makers.internal.dto;

public record PaginationMeta(

        Integer page,
        Integer take,
        Integer itemCount,
        Integer pageCount,
        Boolean hasPreviousPage,
        Boolean hasNextPage
) {
}
