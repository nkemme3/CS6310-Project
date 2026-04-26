package edu.gatech.cs6310.powergrid.service;

import java.math.BigDecimal;
import java.util.Collection;

import org.springframework.stereotype.Service;

import edu.gatech.cs6310.powergrid.domain.EnergySourceType;
import edu.gatech.cs6310.powergrid.domain.Location;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.domain.PowerPlant;
import edu.gatech.cs6310.powergrid.domain.Substation;
import edu.gatech.cs6310.powergrid.domain.Transformer;
import edu.gatech.cs6310.powergrid.error.SystemError;
import edu.gatech.cs6310.powergrid.robustness.JournalCommand;
import edu.gatech.cs6310.powergrid.robustness.ProofService;
import edu.gatech.cs6310.powergrid.robustness.TransactionJournal;

@Service
public class InfrastructureService {

    private final PowerGridSystem pgs;
    private final TransactionJournal journal;

    public InfrastructureService(PowerGridSystem pgs, TransactionJournal journal) {
        this.pgs = pgs;
        this.journal = journal;
    }

    public PowerPlant addPlant(String companyShortName, String plantId, Location location,
                               BigDecimal buildCost, BigDecimal generationCostPerKWh) {
        return addPlant(companyShortName, plantId, location, buildCost, generationCostPerKWh, null);
    }

    public PowerPlant addPlant(String companyShortName, String plantId, Location location,
                               BigDecimal buildCost, BigDecimal generationCostPerKWh,
                               EnergySourceType energySource) {
        ProofService.validateNotBlank("plantId", plantId);
        ProofService.validatePositive("buildCost", buildCost);
        ProofService.validatePositive("generationCostPerKWh", generationCostPerKWh);
        EnergySourceType source = energySource == null ? PowerPlant.DEFAULT_ENERGY_SOURCE : energySource;
        synchronized (pgs.lock()) {
            ProofService.validateExists("Power company", companyShortName, pgs.companies());
            ProofService.validateAvailableId("Power plant", plantId, pgs.plants());
            JournalCommand.AddPlantCmd cmd = new JournalCommand.AddPlantCmd(
                companyShortName, plantId, location, buildCost, generationCostPerKWh, source);
            journal.append(cmd);
            cmd.apply(pgs);
            return pgs.plants().get(plantId);
        }
    }

    public Substation addSubstation(String companyShortName, String substationId, Location location,
                                    BigDecimal buildCost, BigDecimal maintenanceCostPerCycle) {
        ProofService.validateNotBlank("substationId", substationId);
        ProofService.validatePositive("buildCost", buildCost);
        ProofService.validateNonNegative("maintenanceCostPerCycle", maintenanceCostPerCycle);
        synchronized (pgs.lock()) {
            ProofService.validateExists("Power company", companyShortName, pgs.companies());
            ProofService.validateAvailableId("Substation", substationId, pgs.substations());
            JournalCommand.AddSubstationCmd cmd = new JournalCommand.AddSubstationCmd(
                companyShortName, substationId, location, buildCost, maintenanceCostPerCycle);
            journal.append(cmd);
            cmd.apply(pgs);
            return pgs.substations().get(substationId);
        }
    }

    public Transformer addTransformer(String companyShortName, String transformerId, Location location,
                                      BigDecimal installCost, BigDecimal maintenanceCostPerCycle) {
        ProofService.validateNotBlank("transformerId", transformerId);
        ProofService.validatePositive("installCost", installCost);
        ProofService.validateNonNegative("maintenanceCostPerCycle", maintenanceCostPerCycle);
        synchronized (pgs.lock()) {
            ProofService.validateExists("Power company", companyShortName, pgs.companies());
            ProofService.validateAvailableId("Transformer", transformerId, pgs.transformers());
            JournalCommand.AddTransformerCmd cmd = new JournalCommand.AddTransformerCmd(
                companyShortName, transformerId, location, installCost, maintenanceCostPerCycle);
            journal.append(cmd);
            cmd.apply(pgs);
            return pgs.transformers().get(transformerId);
        }
    }

    public void connectPlantToSubstation(String plantId, String substationId) {
        synchronized (pgs.lock()) {
            PowerPlant plant = ProofService.validateExists("Power plant", plantId, pgs.plants());
            Substation sub = ProofService.validateExists("Substation", substationId, pgs.substations());
            if (!plant.getCompanyShortName().equals(sub.getCompanyShortName())) {
                throw SystemError.invalidState("Plant and substation must belong to the same company.");
            }
            if (sub.getSourcePlantId().isPresent()) {
                throw new SystemError(
                    edu.gatech.cs6310.powergrid.error.ErrorCode.ALREADY_CONNECTED,
                    "Substation '" + substationId + "' is already sourced by plant '" + sub.getSourcePlantId().get() + "'.",
                    null,
                    "Disconnect the existing source before connecting a new one."
                );
            }
            ProofService.validateCapacity("Plant '" + plantId + "'", plant.substationCount(), plant.getMaxSubstations());
            ProofService.validateDistance(plant.getLocation(), sub.getLocation(),
                pgs.getMaxPlantSubstationDistance(), "plant-to-substation");
            JournalCommand.ConnectPlantSubstationCmd cmd = new JournalCommand.ConnectPlantSubstationCmd(plantId, substationId);
            journal.append(cmd);
            cmd.apply(pgs);
        }
    }

    public void connectSubstationToTransformer(String substationId, String transformerId) {
        synchronized (pgs.lock()) {
            Substation sub = ProofService.validateExists("Substation", substationId, pgs.substations());
            Transformer t = ProofService.validateExists("Transformer", transformerId, pgs.transformers());
            if (!sub.getCompanyShortName().equals(t.getCompanyShortName())) {
                throw SystemError.invalidState("Substation and transformer must belong to the same company.");
            }
            if (t.getSourceSubstationId().isPresent()) {
                throw new SystemError(
                    edu.gatech.cs6310.powergrid.error.ErrorCode.ALREADY_CONNECTED,
                    "Transformer '" + transformerId + "' is already sourced by substation '" + t.getSourceSubstationId().get() + "'.",
                    null,
                    "Disconnect the existing source before connecting a new one."
                );
            }
            ProofService.validateCapacity("Substation '" + substationId + "'", sub.transformerCount(), sub.getMaxTransformers());
            ProofService.validateDistance(sub.getLocation(), t.getLocation(),
                pgs.getMaxSubstationTransformerDistance(), "substation-to-transformer");
            JournalCommand.ConnectSubstationTransformerCmd cmd = new JournalCommand.ConnectSubstationTransformerCmd(substationId, transformerId);
            journal.append(cmd);
            cmd.apply(pgs);
        }
    }

    public Collection<PowerPlant> listPlants() { return pgs.plants().values(); }
    public Collection<Substation> listSubstations() { return pgs.substations().values(); }
    public Collection<Transformer> listTransformers() { return pgs.transformers().values(); }
}
