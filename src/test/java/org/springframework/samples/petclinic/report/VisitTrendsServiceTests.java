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

import java.time.Clock;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import dev.zazr.collection.Vector;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Drives the pure {@link VisitTrendsService#report(Vector, LocalDate)} core directly with
 * hand-built facts, so the functional aggregation is tested without a database or clock.
 */
class VisitTrendsServiceTests {

	private final VisitTrendsService service = new VisitTrendsService(null, Clock.systemUTC());

	private static VisitFact visit(String date, String type) {
		return new VisitFact(LocalDate.parse(date), type);
	}

	@Test
	void buildsMonthlySeriesSortedChronologically() {
		Vector<VisitFact> facts = Vector.of(visit("2026-05-10", "dog"), visit("2026-03-01", "cat"),
				visit("2026-03-15", "dog"), visit("2026-05-20", "cat"), visit("2026-05-25", "dog"));

		VisitTrendsReport report = this.service.report(facts, LocalDate.parse("2026-12-31"));

		assertThat(report.series().map(TrendPoint::month).asJava()).containsExactly("2026-03", "2026-05");
		assertThat(report.series().map(TrendPoint::visits).asJava()).containsExactly(2L, 3L);
	}

	@Test
	void firstMonthHasNoDeltaThenMonthOverMonthDeltas() {
		Vector<VisitFact> facts = Vector.of(visit("2026-01-02", "dog"), visit("2026-02-02", "dog"),
				visit("2026-02-03", "dog"), visit("2026-02-04", "dog"), visit("2026-03-05", "dog"));

		VisitTrendsReport report = this.service.report(facts, LocalDate.parse("2026-12-31"));
		Vector<TrendPoint> series = report.series();

		// Jan: 1 visit, no predecessor -> absent delta/pct (not a fabricated zero).
		assertThat(series.get(0).delta().isDefined()).isFalse();
		assertThat(series.get(0).changePct().isDefined()).isFalse();
		// Feb: 3 visits, +2 vs Jan's 1 => +200%.
		assertThat(series.get(1).delta().get()).isEqualTo(2L);
		assertThat(series.get(1).changePct().get()).isEqualTo(200.0);
		// Mar: 1 visit, -2 vs Feb's 3.
		assertThat(series.get(2).delta().get()).isEqualTo(-2L);
	}

	@Test
	void byTypeIsCountedAndSortedMostVisitedFirst() {
		Vector<VisitFact> facts = Vector.of(visit("2026-01-01", "dog"), visit("2026-01-02", "dog"),
				visit("2026-01-03", "dog"), visit("2026-01-04", "cat"), visit("2026-01-05", "cat"),
				visit("2026-01-06", "bird"));

		VisitTrendsReport report = this.service.report(facts, LocalDate.parse("2026-12-31"));

		assertThat(report.byType().map(TypeCount::type).asJava()).containsExactly("dog", "cat", "bird");
		assertThat(report.byType().map(TypeCount::visits).asJava()).containsExactly(3L, 2L, 1L);
	}

	@Test
	void warnsAboutFutureDatedVisits() {
		Vector<VisitFact> facts = Vector.of(visit("2026-01-01", "dog"), visit("2026-11-01", "cat"),
				visit("2026-12-01", "cat"));

		VisitTrendsReport report = this.service.report(facts, LocalDate.parse("2026-06-15"));

		assertThat(report.warnings().map(v -> v.message()).asJava())
			.anyMatch(message -> message.contains("2 visit(s) are dated in the future"));
	}

	@Test
	void warnsAboutGapsInTheMonthSeries() {
		// March then July: April, May and June have no visits at all.
		Vector<VisitFact> facts = Vector.of(visit("2026-03-10", "dog"), visit("2026-07-10", "dog"));

		VisitTrendsReport report = this.service.report(facts, LocalDate.parse("2026-12-31"));

		assertThat(report.warnings().map(v -> v.message()).asJava())
			.anyMatch(message -> message.contains("2026-03") && message.contains("2026-07"));
	}

	@Test
	void emptyClinicProducesEmptyReportWithoutErrors() {
		VisitTrendsReport report = this.service.report(Vector.empty(), LocalDate.parse("2026-12-31"));

		assertThat(report.series().isEmpty()).isTrue();
		assertThat(report.byType().isEmpty()).isTrue();
		assertThat(report.warnings().isEmpty()).isTrue();
	}

}
