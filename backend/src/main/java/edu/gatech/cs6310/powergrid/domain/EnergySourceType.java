package edu.gatech.cs6310.powergrid.domain;

import java.math.BigDecimal;

public enum EnergySourceType {
    SOLAR       (new BigDecimal("0.03"), new BigDecimal("0.00"),  true),
    WIND        (new BigDecimal("0.02"), new BigDecimal("0.00"),  true),
    NUCLEAR     (new BigDecimal("0.06"), new BigDecimal("0.005"), false),
    COAL        (new BigDecimal("0.08"), new BigDecimal("0.90"),  false),
    NATURAL_GAS (new BigDecimal("0.07"), new BigDecimal("0.45"),  false);

    private final BigDecimal defaultFuelCostPerKWh;
    private final BigDecimal carbonFactorKgPerKWh;
    private final boolean renewable;

    EnergySourceType(BigDecimal defaultFuelCostPerKWh, BigDecimal carbonFactorKgPerKWh, boolean renewable) {
        this.defaultFuelCostPerKWh = defaultFuelCostPerKWh;
        this.carbonFactorKgPerKWh = carbonFactorKgPerKWh;
        this.renewable = renewable;
    }

    public BigDecimal defaultFuelCostPerKWh() { return defaultFuelCostPerKWh; }
    public BigDecimal carbonFactorKgPerKWh()  { return carbonFactorKgPerKWh; }
    public boolean isRenewable()              { return renewable; }
}
