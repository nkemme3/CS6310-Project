package edu.gatech.cs6310.powergrid.domain;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PowerPlant {

    public static final int DEFAULT_MAX_SUBSTATIONS = 20;
    public static final EnergySourceType DEFAULT_ENERGY_SOURCE = EnergySourceType.NATURAL_GAS;

    private final String plantId;
    private final String companyShortName;
    private Location location;
    private BigDecimal buildCost;
    private BigDecimal generationCostPerKWh;
    private EnergySourceType energySource;
    private int maxSubstations;
    private final Set<String> substationIds = new LinkedHashSet<>();

    @JsonCreator
    public PowerPlant(
        @JsonProperty("plantId") String plantId,
        @JsonProperty("companyShortName") String companyShortName,
        @JsonProperty("location") Location location,
        @JsonProperty("buildCost") BigDecimal buildCost,
        @JsonProperty("generationCostPerKWh") BigDecimal generationCostPerKWh,
        @JsonProperty("energySource") EnergySourceType energySource
    ) {
        this.plantId = plantId;
        this.companyShortName = companyShortName;
        this.location = location;
        this.buildCost = buildCost;
        this.generationCostPerKWh = generationCostPerKWh;
        this.energySource = energySource == null ? DEFAULT_ENERGY_SOURCE : energySource;
        this.maxSubstations = DEFAULT_MAX_SUBSTATIONS;
    }

    public String getPlantId() { return plantId; }
    public String getCompanyShortName() { return companyShortName; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public BigDecimal getBuildCost() { return buildCost; }
    public void setBuildCost(BigDecimal v) { this.buildCost = v; }
    public BigDecimal getGenerationCostPerKWh() { return generationCostPerKWh; }
    public void setGenerationCostPerKWh(BigDecimal v) { this.generationCostPerKWh = v; }
    public EnergySourceType getEnergySource() { return energySource; }
    public void setEnergySource(EnergySourceType v) { this.energySource = v == null ? DEFAULT_ENERGY_SOURCE : v; }
    public int getMaxSubstations() { return maxSubstations; }
    public void setMaxSubstations(int v) { this.maxSubstations = v; }
    public Set<String> getSubstationIds() { return substationIds; }

    public int substationCount() { return substationIds.size(); }
    public boolean hasCapacity() { return substationIds.size() < maxSubstations; }
    public void attachSubstation(String substationId) { substationIds.add(substationId); }
    public void detachSubstation(String substationId) { substationIds.remove(substationId); }

    public BigDecimal carbonFor(BigDecimal kWh) {
        if (kWh == null) return BigDecimal.ZERO;
        return energySource.carbonFactorKgPerKWh().multiply(kWh);
    }
}
