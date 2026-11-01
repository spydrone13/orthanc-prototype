package com.orthanc.orthanc_prototype.assignment.adapter.in.web;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
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

	private static final String BIG_X_SERIES_RULE = """
			{
			  "name": "Big X-series",
			  "priority": 10,
			  "targetMachineId": "ETCH-02",
			  "conditions": [
			    {"attribute": "CHIP_TYPE", "operator": "EQUALS", "values": ["X_SERIES"]},
			    {"attribute": "WAFER_COUNT", "operator": "GREATER_THAN", "values": ["20"]}
			  ]
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void ruleLifecycleAndAutoAssignment() throws Exception {
		String body = mockMvc.perform(post("/api/assignment-rules").contentType(MediaType.APPLICATION_JSON).content(BIG_X_SERIES_RULE))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andExpect(jsonPath("$.enabled").value(true))
			.andExpect(jsonPath("$.conditions", hasSize(2)))
			.andExpect(jsonPath("$.conditions[1].values[0]").value("20"))
			.andReturn().getResponse().getContentAsString();
		String id = JsonPath.read(body, "$.id");

		mockMvc.perform(get("/api/assignment-rules/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Big X-series"));

		mockMvc.perform(autoAssign("X_SERIES", 25))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("ASSIGNED"))
			.andExpect(jsonPath("$.machineId").value("ETCH-02"))
			.andExpect(jsonPath("$.ruleId").value(id));

		mockMvc.perform(autoAssign("X_SERIES", 5))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UNASSIGNED"))
			.andExpect(jsonPath("$.machineId").doesNotExist());

		mockMvc.perform(patch("/api/assignment-rules/{id}/enabled", id).contentType(MediaType.APPLICATION_JSON)
			.content("{\"enabled\": false}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.enabled").value(false));

		mockMvc.perform(autoAssign("X_SERIES", 25)).andExpect(jsonPath("$.status").value("UNASSIGNED"));

		mockMvc.perform(put("/api/assignment-rules/{id}", id).contentType(MediaType.APPLICATION_JSON)
			.content(BIG_X_SERIES_RULE.replace("ETCH-02", "ETCH-01")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.targetMachineId").value("ETCH-01"));

		mockMvc.perform(get("/api/assignment-rules")).andExpect(jsonPath("$", hasSize(1)));

		mockMvc.perform(delete("/api/assignment-rules/{id}", id)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/assignment-rules/{id}", id)).andExpect(status().isNotFound());
	}

	@Test
	void rejectsInvalidRules() throws Exception {
		mockMvc.perform(post("/api/assignment-rules").contentType(MediaType.APPLICATION_JSON)
			.content(BIG_X_SERIES_RULE.replace("\"operator\": \"EQUALS\"", "\"operator\": \"LESS_THAN\"")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("Operator LESS_THAN is not supported for CHIP_TYPE"));

		mockMvc.perform(post("/api/assignment-rules").contentType(MediaType.APPLICATION_JSON)
			.content(BIG_X_SERIES_RULE.replace("[\"X_SERIES\"]", "[\"A100\"]")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("Unknown CHIP_TYPE value 'A100'; allowed: X_SERIES, Z_SERIES, Q_SERIES"));

		mockMvc.perform(post("/api/assignment-rules").contentType(MediaType.APPLICATION_JSON)
			.content(BIG_X_SERIES_RULE.replace("\"EQUALS\"", "\"MATCHES\"")))
			.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/assignment-rules").contentType(MediaType.APPLICATION_JSON)
			.content(BIG_X_SERIES_RULE.replace("ETCH-02", "ETCH-99")))
			.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/assignment-rules").contentType(MediaType.APPLICATION_JSON)
			.content(BIG_X_SERIES_RULE.replace("\"name\": \"Big X-series\",", "")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("name: must not be blank"));

		mockMvc.perform(post("/api/assignment-rules").contentType(MediaType.APPLICATION_JSON)
			.content(BIG_X_SERIES_RULE.replace("CHIP_TYPE", "COLOR")))
			.andExpect(status().isBadRequest());

		mockMvc.perform(get("/api/assignment-rules/not-a-uuid")).andExpect(status().isBadRequest());
	}

	@Test
	void rejectsAutoAssignmentOfLotWithUnknownTypes() throws Exception {
		mockMvc.perform(autoAssign("A100", 25)).andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/lots/auto-assignments").contentType(MediaType.APPLICATION_JSON)
			.content("{\"chipType\": \"X_SERIES\", \"waferCount\": 25}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("processType: must not be null"));
	}

	@Test
	void rejectsDuplicatePriority() throws Exception {
		String rule = BIG_X_SERIES_RULE.replace("\"priority\": 10", "\"priority\": 42");
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
			.andExpect(jsonPath("$.attributes[?(@.name == 'CHIP_TYPE')].kind", contains("ENUMERATED")))
			.andExpect(jsonPath("$.attributes[?(@.name == 'CHIP_TYPE')].values[*]",
					contains("X_SERIES", "Z_SERIES", "Q_SERIES")))
			.andExpect(jsonPath("$.attributes[?(@.name == 'PROCESS_TYPE')].values[*]",
					contains("LEGACY", "STANDARD", "NEXTGEN")))
			.andExpect(jsonPath("$.operators[*].name", not(hasItem("MATCHES"))))
			.andExpect(jsonPath("$.operators[?(@.name == 'NOT_IN')].kinds[*]", contains("ENUMERATED", "NUMERIC")))
			.andExpect(jsonPath("$.operators[?(@.name == 'BETWEEN')].minOperands", contains(2)))
			.andExpect(jsonPath("$.operators[?(@.name == 'BETWEEN')].kinds[*]", contains("NUMERIC")))
			.andExpect(jsonPath("$.operators[?(@.name == 'IN')].maxOperands", contains((Object) null)))
			.andExpect(jsonPath("$.operators[?(@.name == 'EQUALS')].kinds[*]", contains("ENUMERATED", "NUMERIC")));
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
			.content("{\"chipType\": \"" + chipType + "\", \"processType\": \"STANDARD\", \"waferCount\": " + waferCount + "}");
	}

}
