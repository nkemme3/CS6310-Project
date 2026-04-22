package edu.gatech.cs6310.powergrid.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void createCompanyThenReadBack() throws Exception {
        String body = """
            {"longName":"Atlanta Power Co","shortName":"APC_T1","standardRate":0.12}""";
        mvc.perform(post("/api/companies").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.shortName").value("APC_T1"));

        mvc.perform(get("/api/companies/APC_T1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.longName").value("Atlanta Power Co"));
    }

    @Test
    void missingCompanyReturns404WithErrorCode() throws Exception {
        mvc.perform(get("/api/companies/NOPE"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("ENTITY_NOT_FOUND"));
    }

    @Test
    void malformedRequestReturns400() throws Exception {
        mvc.perform(post("/api/companies").contentType(MediaType.APPLICATION_JSON).content("{not json"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_COMMAND"));
    }

    @Test
    void distanceExceededReturns409() throws Exception {
        mvc.perform(post("/api/companies").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"longName":"Atlanta Power Co","shortName":"APC_T2","standardRate":0.12}"""))
            .andExpect(status().isCreated());
        mvc.perform(post("/api/plants").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"companyShortName":"APC_T2","plantId":"P_T2","location":{"x":0,"y":0},
                     "buildCost":1000,"generationCostPerKWh":0.05}"""))
            .andExpect(status().isCreated());
        mvc.perform(post("/api/substations").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"companyShortName":"APC_T2","substationId":"S_T2","location":{"x":100,"y":100},
                     "buildCost":500,"maintenanceCostPerCycle":10}"""))
            .andExpect(status().isCreated());
        mvc.perform(post("/api/connections/plant-substation").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"sourceId":"P_T2","targetId":"S_T2"}"""))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("DISTANCE_EXCEEDED"));
    }
}
