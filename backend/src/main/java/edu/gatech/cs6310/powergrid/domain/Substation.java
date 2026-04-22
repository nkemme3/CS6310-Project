package edu.gatech.cs6310.powergrid.domain;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class Substation {

    public static final int DEFAULT_MAX_TRANSFORMERS = 10;

    private final String substationId;
    private final String companyShortName;
    private Location location;
    private BigDecimal buildCost;
    private BigDecimal maintenanceCostPerCycle;
    private int maxTransformers;
    private String sourcePlantId;
    private final Set<String> transformerIds = new LinkedHashSet<>();

    public Substation(String substationId, String companyShortName, Location location,
                      BigDecimal buildCost, BigDecimal maintenanceCostPerCycle) {
        this.substationId = substationId;
        this.companyShortName = companyShortName;
        this.location = location;
        this.buildCost = buildCost;
        this.maintenanceCostPerCycle = maintenanceCostPerCycle;
        this.maxTransformers = DEFAULT_MAX_TRANSFORMERS;
    }

    public String getSubstationId() { return substationId; }
    public String getCompanyShortName() { return companyShortName; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public BigDecimal getBuildCost() { return buildCost; }
    public BigDecimal getMaintenanceCostPerCycle() { return maintenanceCostPerCycle; }
    public void setMaintenanceCostPerCycle(BigDecimal v) { this.maintenanceCostPerCycle = v; }
    public int getMaxTransformers() { return maxTransformers; }
    public void setMaxTransformers(int v) { this.maxTransformers = v; }
    public Optional<String> getSourcePlantId() { return Optional.ofNullable(sourcePlantId); }
    public void setSourcePlantId(String plantId) { this.sourcePlantId = plantId; }
    public Set<String> getTransformerIds() { return transformerIds; }

    public int transformerCount() { return transformerIds.size(); }
    public boolean hasCapacity() { return transformerIds.size() < maxTransformers; }
    public void attachTransformer(String id) { transformerIds.add(id); }
    public void detachTransformer(String id) { transformerIds.remove(id); }
}
