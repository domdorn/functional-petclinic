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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

import org.springframework.samples.petclinic.functional.Violation;
import org.springframework.stereotype.Service;

import dev.zazr.collection.Map;
import dev.zazr.collection.Vector;
import dev.zazr.control.Option;

/**
 * Builds the clinic-wide visit-trends report from raw {@link VisitFact} rows using
 * nothing but functional transforms. The showcase piece is {@link #trend(Vector)}:
 * pairing each month with the one before it by zipping the series with its own
 * {@code tail()} — no indices, no mutable "previous" accumulator.
 */
@Service
public class VisitTrendsService {

	private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

	private final ReportRepository repository;

	private final Clock clock;

	public VisitTrendsService(ReportRepository repository, Clock clock) {
		this.repository = repository;
		this.clock = clock;
	}

	public VisitTrendsReport build() {
		return report(this.repository.visitFacts(), LocalDate.now(this.clock));
	}

	/**
	 * The pure core: raw facts and a reference "today" in, report out. Package-visible so
	 * it can be driven directly from tests without a database or a real clock.
	 * @param facts every recorded visit
	 * @param today the date used to decide which visits are "in the future"
	 * @return the assembled report
	 */
	VisitTrendsReport report(Vector<VisitFact> facts, LocalDate today) {
		Vector<MonthCount> series = monthlySeries(facts);
		return new VisitTrendsReport(trend(series), byType(facts), warnings(facts, series, today));
	}

	private Vector<MonthCount> monthlySeries(Vector<VisitFact> facts) {
		Map<YearMonth, Long> counts = facts.groupMapReduce(fact -> YearMonth.from(fact.date()), fact -> 1L, Long::sum);
		return facts.map(fact -> YearMonth.from(fact.date()))
			.distinct()
			.sorted()
			.map(month -> new MonthCount(month, counts.getOrElse(month, 0L)));
	}

	private Vector<TrendPoint> trend(Vector<MonthCount> series) {
		// The first month is just its bare point — it has no predecessor to compare to.
		Vector<TrendPoint> head = series.take(1).map(this::bare);
		// zip the series with itself shifted by one => consecutive (previous, current)
		// pairs. drop(1) rather than tail() so an empty series stays empty instead of
		// throwing. Each current month's bare point is then enriched with its comparison.
		Vector<TrendPoint> rest = series.zip(series.drop(1)).map(pair -> compareToPrevious(pair._1(), pair._2()));
		return head.appendAll(rest);
	}

	/** A month's point before any comparison: the intrinsic month + visit count. */
	private TrendPoint bare(MonthCount month) {
		return new TrendPoint(MONTH.format(month.month()), month.visits(), Option.none(), Option.none());
	}

	/**
	 * Enrich the current month's bare point with how it compares to the previous month.
	 */
	private TrendPoint compareToPrevious(MonthCount previous, MonthCount current) {
		long delta = current.visits() - previous.visits();
		Option<Double> changePct = (previous.visits() == 0) ? Option.none()
				: Option.some(delta * 100.0 / previous.visits());
		return bare(current).withDelta(Option.some(delta)).withChangePct(changePct);
	}

	private Vector<TypeCount> byType(Vector<VisitFact> facts) {
		Map<String, Long> counts = facts.groupMapReduce(VisitFact::petType, fact -> 1L, Long::sum);
		return facts.map(VisitFact::petType)
			.distinct()
			.map(type -> new TypeCount(type, counts.getOrElse(type, 0L)))
			.sortBy(Comparator.<Long>reverseOrder(), TypeCount::visits);
	}

	private Vector<Violation> warnings(Vector<VisitFact> facts, Vector<MonthCount> series, LocalDate today) {
		Vector<Violation> result = Vector.empty();

		long future = facts.filter(fact -> fact.date().isAfter(today)).size();
		if (future > 0) {
			result = result.append(Violation.global("visits.future", future + " visit(s) are dated in the future"));
		}

		// Gaps are the same zip-with-tail trick: a jump of more than one month between
		// consecutive recorded months means some months in between have no visits at all.
		Vector<YearMonth> months = series.map(MonthCount::month);
		Vector<Violation> gaps = months.zip(months.drop(1))
			.filter(pair -> ChronoUnit.MONTHS.between(pair._1(), pair._2()) > 1)
			.map(pair -> Violation.global("series.gap",
					"no visits recorded between " + MONTH.format(pair._1()) + " and " + MONTH.format(pair._2())));

		return result.appendAll(gaps);
	}

}
