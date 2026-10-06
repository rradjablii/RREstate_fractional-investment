package com.rrestate.portfolio;

import com.rrestate.common.MoneyRules;
import com.rrestate.distribution.Allocation;
import com.rrestate.distribution.AllocationRepository;
import com.rrestate.property.Property;
import com.rrestate.property.PropertyImage;
import com.rrestate.property.PropertyImageRepository;
import com.rrestate.property.PropertyRepository;
import com.rrestate.transaction.Transaction;
import com.rrestate.transaction.TransactionRepository;
import com.rrestate.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authoritative portfolio numbers, computed from confirmed positions and the immutable ledger.
 * Totals, allocation and performance cover ONE currency (the requested one, else the currency with the
 * largest invested amount); the positions list always shows every position with its own currency.
 * "Current value" is valued at cost (invested amount) until a valuation model exists.
 */
@Service
public class PortfolioService {
    public record Slice(UUID propertyId, String propertyName, String percent) {}
    public record Point(LocalDate date, String value) {}
    public record PositionResponse(UUID propertyId, String propertyName, String country, String coverImageUrl, String currency,
                                   String investedAmount, String interestPercent, String monthlyDistribution, String status) {}
    public record PortfolioResponse(String currency, String totalInvested, String totalDistributions, String currentValue, String totalReturnPercent,
                                    List<Slice> allocation, List<Point> performance, List<PositionResponse> positions) {}

    private static final String DEFAULT_CURRENCY = "USD";

    private final PositionRepository positions;
    private final PropertyRepository properties;
    private final PropertyImageRepository images;
    private final AllocationRepository allocations;
    private final TransactionRepository transactions;

    public PortfolioService(PositionRepository positions, PropertyRepository properties, PropertyImageRepository images,
                            AllocationRepository allocations, TransactionRepository transactions) {
        this.positions = positions; this.properties = properties; this.images = images; this.allocations = allocations; this.transactions = transactions;
    }

    @Transactional(readOnly = true)
    public PortfolioResponse forUser(UUID userId, String requestedCurrency) {
        List<Position> all = positions.findByUserId(userId);
        Map<UUID, Property> props = properties.findAllById(all.stream().map(Position::getPropertyId).toList()).stream().collect(Collectors.toMap(Property::getId, p -> p));
        Map<UUID, String> covers = new HashMap<>();
        for (PropertyImage i : images.findByPropertyIdInOrderBySortOrderAsc(props.keySet())) covers.putIfAbsent(i.getPropertyId(), i.getUrl());
        Map<UUID, BigDecimal> latestPaid = new HashMap<>();
        for (Allocation a : allocations.findByUserIdAndStatusOrderByPaidAtDesc(userId, "PAID")) latestPaid.putIfAbsent(a.getPositionId(), a.getAmount());

        String currency = requestedCurrency != null ? requestedCurrency : all.stream()
                .collect(Collectors.groupingBy(Position::getCurrency, Collectors.reducing(BigDecimal.ZERO, Position::getInvestedAmount, BigDecimal::add)))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(DEFAULT_CURRENCY);

        List<Position> inCurrency = all.stream().filter(p -> p.getCurrency().equals(currency)).toList();
        BigDecimal invested = inCurrency.stream().map(Position::getInvestedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal distributions = allocations.findByUserIdAndStatusOrderByPaidAtDesc(userId, "PAID").stream()
                .filter(a -> a.getCurrency().equals(currency)).map(Allocation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Slice> allocation = inCurrency.stream().map(p -> new Slice(p.getPropertyId(), props.get(p.getPropertyId()).getName(),
                MoneyRules.str(MoneyRules.percentOf(p.getInvestedAmount(), invested, MoneyRules.PERCENT_SCALE)))).toList();

        List<PositionResponse> rows = all.stream().map(p -> {
            Property prop = props.get(p.getPropertyId());
            BigDecimal monthly = latestPaid.get(p.getId());
            return new PositionResponse(prop.getId(), prop.getName(), prop.getCountry(), covers.get(prop.getId()), p.getCurrency(),
                    MoneyRules.str(p.getInvestedAmount()), MoneyRules.str(p.getInterestPercent()), MoneyRules.str(monthly), "ACTIVE");
        }).toList();

        return new PortfolioResponse(currency, MoneyRules.str(invested), MoneyRules.str(distributions), MoneyRules.str(invested),
                invested.signum() == 0 ? null : MoneyRules.str(MoneyRules.percentOf(distributions, invested, MoneyRules.PERCENT_SCALE)),
                allocation, performance(userId, currency), rows);
    }

    /** Cumulative (confirmed investments + paid distributions) per day, from the ledger. */
    private List<Point> performance(UUID userId, String currency) {
        List<Transaction> events = transactions.findByUserIdAndTypeInAndStatusAndCurrencyOrderByCreatedAtAsc(
                userId, List.of(TransactionType.INVESTMENT_CONFIRMED, TransactionType.DISTRIBUTION_PAID), "COMPLETED", currency);
        TreeMap<LocalDate, BigDecimal> byDay = new TreeMap<>();
        BigDecimal running = BigDecimal.ZERO;
        for (Transaction t : events) {
            running = running.add(t.getAmount());
            byDay.put(t.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate(), running);
        }
        return byDay.entrySet().stream().map(e -> new Point(e.getKey(), MoneyRules.str(e.getValue()))).toList();
    }
}
