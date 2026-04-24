package edu.gatech.cs6310.powergrid.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.gatech.cs6310.powergrid.api.Dtos.ConnectRequest;
import edu.gatech.cs6310.powergrid.api.Dtos.CreatePlantRequest;
import edu.gatech.cs6310.powergrid.api.Dtos.CreateSubstationRequest;
import edu.gatech.cs6310.powergrid.api.Dtos.CreateTransformerRequest;
import edu.gatech.cs6310.powergrid.api.Dtos.PlantView;
import edu.gatech.cs6310.powergrid.api.Dtos.SubstationView;
import edu.gatech.cs6310.powergrid.api.Dtos.TransformerView;
import edu.gatech.cs6310.powergrid.service.InfrastructureService;

@RestController
@RequestMapping("/api")
public class InfrastructureController {

    private final InfrastructureService service;

    public InfrastructureController(InfrastructureService service) {
        this.service = service;
    }

    @GetMapping("/plants")
    public List<PlantView> listPlants() {
        return service.listPlants().stream().map(PlantView::from).toList();
    }

    @PostMapping("/plants")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<PlantView> createPlant(@RequestBody CreatePlantRequest req) {
        PlantView v = PlantView.from(service.addPlant(
            req.companyShortName(), req.plantId(),
            req.location() == null ? null : req.location().toDomain(),
            req.buildCost(), req.generationCostPerKWh(), req.energySource()
        ));
        return ResponseEntity.status(201).body(v);
    }

    @GetMapping("/substations")
    public List<SubstationView> listSubstations() {
        return service.listSubstations().stream().map(SubstationView::from).toList();
    }

    @PostMapping("/substations")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<SubstationView> createSubstation(@RequestBody CreateSubstationRequest req) {
        SubstationView v = SubstationView.from(service.addSubstation(
            req.companyShortName(), req.substationId(),
            req.location() == null ? null : req.location().toDomain(),
            req.buildCost(), req.maintenanceCostPerCycle()
        ));
        return ResponseEntity.status(201).body(v);
    }

    @GetMapping("/transformers")
    public List<TransformerView> listTransformers() {
        return service.listTransformers().stream().map(TransformerView::from).toList();
    }

    @PostMapping("/transformers")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<TransformerView> createTransformer(@RequestBody CreateTransformerRequest req) {
        TransformerView v = TransformerView.from(service.addTransformer(
            req.companyShortName(), req.transformerId(),
            req.location() == null ? null : req.location().toDomain(),
            req.installCost(), req.maintenanceCostPerCycle()
        ));
        return ResponseEntity.status(201).body(v);
    }

    @PostMapping("/connections/plant-substation")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<Void> connectPlantSubstation(@RequestBody ConnectRequest req) {
        service.connectPlantToSubstation(req.sourceId(), req.targetId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/connections/substation-transformer")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<Void> connectSubstationTransformer(@RequestBody ConnectRequest req) {
        service.connectSubstationToTransformer(req.sourceId(), req.targetId());
        return ResponseEntity.noContent().build();
    }
}
