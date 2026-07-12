package com.example.eumbev2.common.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Generic `{ items, page }` wrapper used by every `*Page` response schema
 * (ClassSummaryPage, MemberPage, AssignmentPage, SubmissionPage, NoticePage, WaitingMemberPage...).
 */
public record PageResponse<T>(List<T> items, PageMetaResponse page) {

    public static <T> PageResponse<T> of(List<T> items, PageMetaResponse page) {
        return new PageResponse<>(items, page);
    }

    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        List<T> items = page.getContent().stream().map(mapper).toList();
        return new PageResponse<>(items, PageMetaResponse.from(page));
    }
}
