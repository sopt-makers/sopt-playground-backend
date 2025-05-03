package org.sopt.makers.internal.external.makers;

public record PaginationMeta(

        Integer page,

        Integer take,

        Integer itemCount,

        Integer pageCount,

        Boolean hasPreviousPage,

        Boolean hasNextPage
) {
}
