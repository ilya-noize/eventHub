package com.event.hub.filter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public class PageableFilter {
    public static final int PAGE_SIZE_MINIMAL = 3;

    @Min(0)
    private Integer pageNumber;

    @Min(PAGE_SIZE_MINIMAL)
    @Max(100)
    private Integer pageSize;

    public Pageable toPageable() {
        int size = Optional.ofNullable(pageSize).orElse(PAGE_SIZE_MINIMAL);
        int page = Optional.ofNullable(pageNumber).orElse(0);
        return Pageable.ofSize(size)
                .withPage(page);
    }
}
