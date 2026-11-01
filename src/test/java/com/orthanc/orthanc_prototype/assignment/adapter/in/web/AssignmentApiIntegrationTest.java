package com.orthanc.orthanc_prototype.assignment.adapter.in.web;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:assignment-api;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@DirtiesContext
@Transactional
class AssignmentApiIntegrationTest {

	private static final String BIG_A100_RULE = """
			{
			  "name": "Big A100",
			  "priority": 10,
			  "targetMachineId": "ETCH-02",
			  "conditions": [
			    {"attribute": "CHIP_TYPE", "operator": "EQUALS", "values": ["A100"]},
			    {"attribute": "WAFER_COUNT", "operator": "GREATER_THAN", "values": ["20"]}
			  ]
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void ruleLifecycleAndAutoAssignment() throws Exception {
		String body = mockMvc.perform(post("/api/assignment-rules").contentType(MediaType.APPLICATION_JSON).content(BIG_A100_RULE))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andExpect(jsonPath("$.enabled").value(true))
			.andExpect(jsonPath("$.conditions", hasSize(2)))
			.andExpect(jsonPath("$.conditions[1].values[0]").value("20"))
			.andReturn().getResponse().getContentAsString();
		String id = JsonPath.read(body, "$.id");

		mockMvc.perform(get("/api/assignment-rules/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Big A100"));

		mockMvc.perform(autoAssign("A100", 25))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("ASSIGNED"))
			.andExpect(jsonPath("$.machineId").value("ETCH-02"))
			.andExpect(jsonPath("$.ruleId").value(id));

		mockMvc.perform(autoAssign("A100", 5))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UNASSIGNED"))
			.andExpect(jsonPath("$.machineId").doesNotExist());

		mockMvc.perform(patch("/api/assignment-rules/{id}/enabled", id).contentType(MediaType.APPLICATION_JSON)
			.content("{\"enabled\": false}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.enabled").value(false));

		mockMvc.perform(autoAssign("A100", 25)).andExpect(jsonPath("$.status").value("UNASSIGNED"));

		mockMvc.perform(put("/api/assignment-rules/{id}", id).contentType(MediaType.APPLICATION_JSON)
			.content(BIG_A100_RULE.replace("ETCH-02", "ETCH-01")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.targetMachineId").value("ETCH-01"));

		mockMvc.perform(get("/api/assignment-rules")).andExpect(jsonPath("$", hasSize(1)));

		mockMvc.perform(delete("/api/assignment-rules/{id}", id)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/assignment-rules/{id}", id)).andExpect(status().isNotFound());
	}

	@Test
	void rejectsInvalidRules() throws Exception {
		mockMvc.perform(post("/api/assignment-rules").contentType(MediaType.APPLICATION_JSON)
			.content(BIG_A100_RULE.replace("\"GREATER_THAN\"", "\"STARTS_WITH\"")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("Operator STARTS_WITH is not supported for WAFER_COUNT"));

		mockMvc.perform(post("/api/assignment-rules").contentType(MediaType.APPLICATION_JSON)
			.content(BIG_A100_RULE.replace("ETCH-02", "ETCH-99")))
			.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/assignment-rules").contentType(MediaType.APPLICATION_JSON)
			.content(BIG_A100_RULE.replace("\"name\": \"Big A100\",", "")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("name: must not be blank"));

		mockMvc.perform(post("/api/assignment-rules").contentType(MediaType.APPLICATION_JSON)
			.content(BIG_A100_RULE.replace("CHIP_TYPE", "COLOR")))
			.andExpect(status().isBadRequest());

		mockMvc.perform(get("/api/assignment-rules/not-a-uuid")).andExpect(status().isBadRequest());
	}

	@Test
	void rejectsDuplicatePriority() throws Exception {
		String rule = BIG_A100_RULE.replace("\"priority\": 10", "\"priority\": 42");
		mockMvc.perform(post("/api/assignment-rules").contentType(MediaType.APPLICATION_JSON).content(rule))
			.andExpect(status().isCreated());

		mockMvc.perform(post("/api/assignment-rules").contentType(MediaType.APPLICATION_JSON).content(rule))
			.andExpect(status().isConflict());
	}

	@Test
	void exposesMachinesAndConditionTypesForTheUi() throws Exception {
		mockMvc.perform(get("/api/machines"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].id", contains("ETCH-01", "ETCH-02")))
			.andExpect(jsonPath("$[0].name").value("Plasma Etcher 1"));

		mockMvc.perform(get("/api/assignment-rules/condition-types"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.attributes[?(@.name == 'WAFER_COUNT')].kind", contains("NUMERIC")))
			.andExpect(jsonPath("$.operators[?(@.name == 'BETWEEN')].minOperands", contains(2)))
			.andExpect(jsonPath("$.operators[?(@.name == 'BETWEEN')].kinds[*]", contains("NUMERIC")))
			.andExpect(jsonPath("$.operators[?(@.name == 'IN')].maxOperands", contains((Object) null)))
			.andExpect(jsonPath("$.operators[?(@.name == 'EQUALS')].kinds[*]", contains("TEXT", "NUMERIC")));
	}

	@Test
	void servesRulesUi() throws Exception {
		mockMvc.perform(get("/rules.html")).andExpect(status().isOk());
		mockMvc.perform(get("/js/rules.js")).andExpect(status().isOk());
		mockMvc.perform(get("/webjars/bootstrap/css/bootstrap.min.css")).andExpect(status().isOk());
		mockMvc.perform(get("/webjars/bootstrap/js/bootstrap.bundle.min.js")).andExpect(status().isOk());
	}

	private static org.springframework.test.web.servlet.RequestBuilder autoAssign(String chipType, int waferCount) {
		return post("/api/lots/auto-assignments").contentType(MediaType.APPLICATION_JSON)
			.content("{\"chipType\": \"" + chipType + "\", \"processType\": \"CMOS-7nm\", \"waferCount\": " + waferCount + "}");
	}

}
