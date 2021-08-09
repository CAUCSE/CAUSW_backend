package net.causw.adapter.web;

import net.causw.application.CircleService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/circles")
public class CircleController {
    private final CircleService circleService;

    public CircleController(CircleService circleService) {
        this.circleService = circleService;
    }

    /* TODO : Refactoring & Implementation
    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public CircleDto findById(@PathVariable String id) {
        return this.circleService.findById(id);
    }
     */
}
