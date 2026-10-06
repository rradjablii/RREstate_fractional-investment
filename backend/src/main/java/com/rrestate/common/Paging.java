package com.rrestate.common;

import com.rrestate.config.AppProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/** Bounded pagination; sort is always chosen server-side. */
@Component
public class Paging {
    private final int maxSize;

    public Paging(AppProperties props) { this.maxSize = props.pagination().maxSize(); }

    public PageRequest of(int page, int size, Sort sort) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), maxSize), sort);
    }
}
