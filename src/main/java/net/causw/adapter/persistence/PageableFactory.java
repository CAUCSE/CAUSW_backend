package net.causw.adapter.persistence;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class PageableFactory {
    private static final Integer PAGE_SIZE = 10;

    public Pageable create(Integer pageNumber) {
        return PageRequest.of(pageNumber, PAGE_SIZE);
    }
}
