package com.rrestate.document;

import com.rrestate.common.PageResponse;
import com.rrestate.common.Paging;
import com.rrestate.portfolio.Position;
import com.rrestate.portfolio.PositionRepository;
import com.rrestate.property.Property;
import com.rrestate.property.PropertyDocument;
import com.rrestate.property.PropertyRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Metadata for documents of properties the caller has a position in. File download is not implemented yet. */
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {
    public record DocumentItem(UUID id, String title, String category, String propertyName, Instant createdAt) {}

    private final PositionRepository positions;
    private final PropertyRepository properties;
    private final Paging paging;

    public DocumentController(PositionRepository positions, PropertyRepository properties, Paging paging) {
        this.positions = positions; this.properties = properties; this.paging = paging;
    }

    @GetMapping
    @Transactional(readOnly = true)
    PageResponse<DocumentItem> list(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        UUID userId = UUID.fromString(jwt.getSubject());
        List<DocumentItem> items = properties.findAllById(positions.findByUserId(userId).stream().map(Position::getPropertyId).toList()).stream()
                .flatMap(p -> p.getDocuments().stream().map(d -> item(p, d)))
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt())).toList();
        PageRequest pr = paging.of(page, size, org.springframework.data.domain.Sort.unsorted());
        int from = (int) Math.min(pr.getOffset(), items.size());
        int to = Math.min(from + pr.getPageSize(), items.size());
        return PageResponse.of(new PageImpl<>(items.subList(from, to), pr, items.size()), x -> x);
    }

    private static DocumentItem item(Property p, PropertyDocument d) {
        return new DocumentItem(d.getId(), d.getTitle(), d.getCategory(), p.getName(), d.getCreatedAt());
    }
}
