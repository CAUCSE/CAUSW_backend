package net.causw.adapter.persistence;

import net.causw.domain.model.StaticValue;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class PageableFactory {
    public Pageable create(Integer pageNumber) {
        return PageRequest.of(pageNumber, StaticValue.DEFAULT_PAGE_SIZE);
    }

    public Pageable create(Integer pageNumber, Integer pageSize) {
        return PageRequest.of(pageNumber, pageSize);
    }
}
