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

import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.functional.DomainError;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import dev.zazr.control.Either;

/**
 * Serves the reports. The page itself is a near-empty Thymeleaf shell; it fetches its
 * data over XHR from the JSON endpoint, which returns the house-style {@code Either}
 * (rendered as JSON on the right, or an RFC-7807 problem on the left by the shared
 * {@code EitherReturnValueHandler}).
 */
@Controller
class ReportController {

	private final VisitTrendsService visitTrends;

	ReportController(VisitTrendsService visitTrends) {
		this.visitTrends = visitTrends;
	}

	@GetMapping("/reports")
	public String reportsPage() {
		return "reports/visitTrends";
	}

	@GetMapping(path = "/api/reports/visit-trends", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Either<DomainError, VisitTrendsReport> visitTrends() {
		return Either.right(this.visitTrends.build());
	}

}
