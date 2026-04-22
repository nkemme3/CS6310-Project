package edu.gatech.cs6310.powergrid.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record LedgerEntry(
    long entryId,
    String companyShortName,
    LedgerEntryType entryType,
    LocalDate periodStart,
    LocalDate periodEnd,
    BigDecimal kWh,
    BigDecimal amount,
    String description,
    Map<String, String> relatedIds
) {
}
