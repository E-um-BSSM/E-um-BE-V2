package com.example.eumbev2.common.response;

import org.springframework.data.domain.Page;

/**
 * Mirrors the API spec's `PageMeta` schema. Included in every paginated list response.
 */
public record PageMetaResponse(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static PageMetaResponse from(Page<?> page) {
        return new PageMetaResponse(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
