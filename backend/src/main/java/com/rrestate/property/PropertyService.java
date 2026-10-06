package com.rrestate.property;

import com.rrestate.audit.AuditService;
import com.rrestate.common.ApiException;
import com.rrestate.common.MoneyRules;
import com.rrestate.common.PageResponse;
import com.rrestate.common.Paging;
import com.rrestate.config.AppProperties;
import com.rrestate.property.PropertyDtos.*;
import jakarta.persistence.criteria.Predicate;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropertyService {
    private final PropertyRepository properties;
    private final OpportunityRepository opportunities;
    private final PropertyImageRepository images;
    private final AuditService audit;
    private final AppProperties props;
    private final Paging paging;
    private final Clock clock;

    public PropertyService(PropertyRepository properties, OpportunityRepository opportunities, PropertyImageRepository images,
                           AuditService audit, AppProperties props, Paging paging, Clock clock) {
        this.properties = properties; this.opportunities = opportunities; this.images = images;
        this.audit = audit; this.props = props; this.paging = paging; this.clock = clock;
    }

    /** Public listing: drafts are never visible. */
    @Transactional(readOnly = true)
    public PageResponse<PropertySummary> listPublic(int page, int size, String country, PropertyStatus status) {
        Specification<Property> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.notEqual(root.get("status"), PropertyStatus.DRAFT));
            if (country != null && !country.isBlank()) ps.add(cb.equal(root.get("country"), country));
            if (status != null) ps.add(cb.equal(root.get("status"), status));
            return cb.and(ps.toArray(Predicate[]::new));
        };
        return summaries(properties.findAll(spec, paging.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @Transactional(readOnly = true)
    public PageResponse<PropertySummary> listAll(int page, int size) {
        return summaries(properties.findAll(paging.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @Transactional(readOnly = true)
    public PropertyDetail getPublic(UUID id) {
        Property p = properties.findById(id).filter(x -> x.getStatus() != PropertyStatus.DRAFT).orElseThrow(() -> ApiException.notFound("Property"));
        Opportunity o = opportunities.findByPropertyId(id).orElse(null);
        return detail(p, o);
    }

    @Transactional
    public PropertyDetail create(UUID actor, CreatePropertyRequest r) {
        if (!MoneyRules.hasAtMostInputScale(r.minimumInvestment()) || r.minimumInvestment().compareTo(r.targetRaise()) > 0) {
            throw ApiException.unprocessable("INVALID_OFFERING", "Minimum investment cannot exceed the target raise.");
        }
        Instant now = Instant.now(clock);
        Property p = new Property(r.name().trim(), r.country().trim(), r.city().trim(), r.address(), r.propertyType().trim(), r.description(),
                r.status(), r.currency(), r.acquisitionPrice(), r.valuation(), r.expectedYieldPercent(), r.projectedNetIncome(),
                r.legalStructure(), r.entityName(), now);
        int i = 0;
        for (ImageInput im : nullSafe(r.images())) p.getImages().add(new PropertyImage(im.url(), im.caption(), i++));
        for (DocumentInput d : nullSafe(r.documents())) p.getDocuments().add(new PropertyDocument(d.title(), d.category(), null, now));
        i = 0;
        for (FinancialLineInput f : nullSafe(r.financials())) p.getFinancialLines().add(new FinancialLine(f.label(), f.amount(), f.basis(), i++));
        p.getRisks().addAll(nullSafe(r.risks()));
        p.getDisclosures().addAll(nullSafe(r.disclosures()));
        properties.saveAndFlush(p);
        Opportunity o = opportunities.save(new Opportunity(p.getId(), r.minimumInvestment(), r.targetRaise(), r.investmentEnabled()));
        audit.record(actor, "PROPERTY_CREATED", "Property", p.getId(), p.getName());
        return detail(p, o);
    }

    @Transactional
    public void updateStatus(UUID actor, UUID id, PropertyStatus status) {
        Property p = properties.findById(id).orElseThrow(() -> ApiException.notFound("Property"));
        p.setStatus(status);
        p.setUpdatedAt(Instant.now(clock));
        audit.record(actor, "PROPERTY_STATUS_CHANGED", "Property", id, status.name());
    }

    // ---- mapping ----

    private PageResponse<PropertySummary> summaries(Page<Property> page) {
        List<UUID> ids = page.getContent().stream().map(Property::getId).toList();
        Map<UUID, Opportunity> opps = opportunities.findByPropertyIdIn(ids).stream().collect(Collectors.toMap(Opportunity::getPropertyId, Function.identity()));
        Map<UUID, String> covers = new java.util.HashMap<>();
        for (PropertyImage im : images.findByPropertyIdInOrderBySortOrderAsc(ids)) covers.putIfAbsent(im.getPropertyId(), im.getUrl());
        return PageResponse.of(page, p -> summary(p, opps.get(p.getId()), covers.get(p.getId())));
    }

    private PropertySummary summary(Property p, Opportunity o, String cover) {
        return new PropertySummary(p.getId(), p.getName(), p.getCountry(), p.getCity(), p.getPropertyType(), p.getStatus(), cover, p.getCurrency(),
                MoneyRules.str(p.getExpectedYieldPercent()),
                o == null ? null : MoneyRules.str(o.getMinimumInvestment()), o == null ? null : MoneyRules.str(o.getTargetRaise()),
                o == null ? "0" : MoneyRules.str(o.getRaisedAmount()),
                o == null ? "0" : MoneyRules.str(MoneyRules.percentOf(o.getRaisedAmount(), o.getTargetRaise(), MoneyRules.PERCENT_SCALE)));
    }

    private PropertyDetail detail(Property p, Opportunity o) {
        String cover = p.getImages().stream().findFirst().map(PropertyImage::getUrl).orElse(null);
        PropertySummary s = summary(p, o, cover);
        return new PropertyDetail(p.getId(), p.getName(), p.getCountry(), p.getCity(), p.getPropertyType(), p.getStatus(), cover, p.getCurrency(),
                s.expectedYieldPercent(), s.minimumInvestment(), s.targetRaise(), s.raisedAmount(), s.fundingProgressPercent(),
                p.getDescription(), p.getAddress(), MoneyRules.str(p.getAcquisitionPrice()), MoneyRules.str(p.getValuation()),
                o == null ? null : o.getId(), o != null && o.isInvestmentEnabled() && props.compliance().investmentEnabled(),
                p.getLegalStructure(), p.getEntityName(),
                p.getFinancialLines().stream().sorted(Comparator.comparingInt(FinancialLine::getSortOrder)).map(f -> new FinancialLineDto(f.getLabel(), MoneyRules.str(f.getAmount()), f.getBasis())).toList(),
                MoneyRules.str(p.getProjectedNetIncome()),
                p.getImages().stream().map(i -> new ImageDto(i.getId(), i.getUrl(), i.getCaption())).toList(),
                List.copyOf(p.getRisks()), List.copyOf(p.getDisclosures()),
                p.getDocuments().stream().map(d -> new DocumentRef(d.getId(), d.getTitle(), d.getCategory(), d.getSizeBytes())).toList());
    }

    private static <T> List<T> nullSafe(List<T> l) { return l == null ? List.of() : l; }
}
