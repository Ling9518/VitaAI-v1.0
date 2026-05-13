package com.vitaai.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    private List<T> list;
    private Pagination pagination;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Pagination {
        private int page;
        private int pageSize;
        private long total;
        private int totalPages;
    }

    public static <T> PageResponse<T> of(List<T> list, int page, int pageSize, long total) {
        return PageResponse.<T>builder()
                .list(list)
                .pagination(Pagination.builder()
                        .page(page)
                        .pageSize(pageSize)
                        .total(total)
                        .totalPages((int) Math.ceil((double) total / pageSize))
                        .build())
                .build();
    }
}
