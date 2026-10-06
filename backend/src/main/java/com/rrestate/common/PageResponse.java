package com.rrestate.common;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public static <E, T> PageResponse<T> of(Page<E> p, Function<E, T> map) {
        return new PageResponse<>(p.getContent().stream().map(map).toList(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }
}
