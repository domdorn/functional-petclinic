/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.report;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.functional.Violation;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.zazr.collection.Vector;
import dev.zazr.control.Option;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Web-slice test for the reports: the page is a plain view, and the JSON endpoint renders
 * the {@code Either}'s right side (including zazr {@link Vector}/{@link Option}) as JSON.
 */
@WebMvcTest(ReportController.class)
class ReportControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private VisitTrendsService visitTrends;

	@Test
	void reportsPageRendersView() throws Exception {
		this.mockMvc.perform(get("/reports")).andExpect(status().isOk()).andExpect(view().name("reports/visitTrends"));
	}

	@Test
	void visitTrendsEndpointRendersJson() throws Exception {
		VisitTrendsReport report = new VisitTrendsReport(
				Vector.of(new TrendPoint("2026-03", 2, Option.none(), Option.none()),
						new TrendPoint("2026-04", 5, Option.some(3L), Option.some(150.0))),
				Vector.of(new TypeCount("dog", 4), new TypeCount("cat", 3)),
				Vector.of(Violation.global("visits.future", "1 visit(s) are dated in the future")));
		given(this.visitTrends.build()).willReturn(report);

		this.mockMvc.perform(get("/api/reports/visit-trends").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			// first month: absent delta -> JSON null (Option.none rendered by
			// zazr-jackson)
			.andExpect(jsonPath("$.series[0].month").value("2026-03"))
			.andExpect(jsonPath("$.series[0].delta").value(org.hamcrest.Matchers.nullValue()))
			// second month: present delta and percentage
			.andExpect(jsonPath("$.series[1].delta").value(3))
			.andExpect(jsonPath("$.series[1].changePct").value(150.0))
			.andExpect(jsonPath("$.byType[0].type").value("dog"))
			.andExpect(jsonPath("$.byType[0].visits").value(4))
			.andExpect(jsonPath("$.warnings[0].message").value("1 visit(s) are dated in the future"));
	}

}
