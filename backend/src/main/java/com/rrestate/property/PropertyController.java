package com.rrestate.property;

import com.rrestate.common.PageResponse;
import com.rrestate.property.PropertyDtos.PropertyDetail;
import com.rrestate.property.PropertyDtos.PropertySummary;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {
    private final PropertyService service;

    public PropertyController(PropertyService service) { this.service = service; }

    @GetMapping
    PageResponse<PropertySummary> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size,
                                       @RequestParam(required = false) String country, @RequestParam(required = false) PropertyStatus status) {
        return service.listPublic(page, size, country, status);
    }

    @GetMapping("/{id}")
    PropertyDetail get(@PathVariable UUID id) { return service.getPublic(id); }
}
