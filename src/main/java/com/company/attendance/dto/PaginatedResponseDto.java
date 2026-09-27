package com.company.attendance.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Generic immutable Java 21 Record for paginated API responses. Guarantees 100% adherence to the
 * OpenAPI contract.
 *
 * @param <T> The payload type contained in the page
 */
public record PaginatedResponseDto<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public PaginatedResponseDto {
        content =
                (content == null)
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(new ArrayList<>(content));
    }

    /**
     * Factory method creating a PaginatedResponseDto from a Spring Data Page. Ensures that when
     * totalElements is 0, totalPages is also strictly 0.
     *
     * @param springPage Spring Data Page to adapt
     * @param <T> Element type
     * @return Immutable PaginatedResponseDto
     */
    public static <T> PaginatedResponseDto<T> of(Page<T> springPage) {
        if (springPage == null || springPage.getTotalElements() == 0) {
            return new PaginatedResponseDto<>(
                    Collections.emptyList(),
                    springPage != null ? springPage.getNumber() : 0,
                    springPage != null ? springPage.getSize() : 10,
                    0L,
                    0,
                    true,
                    true);
        }

        return new PaginatedResponseDto<>(
                springPage.getContent(),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements(),
                springPage.getTotalPages(),
                springPage.isFirst(),
                springPage.isLast());
    }
}
