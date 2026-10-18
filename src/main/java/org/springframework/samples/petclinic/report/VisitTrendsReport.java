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

import org.springframework.samples.petclinic.functional.Violation;

import dev.zazr.collection.Vector;

/**
 * The clinic-wide visit-trends report returned as JSON. Every collection is a zazr
 * {@link Vector} rendered directly by zazr-jackson.
 *
 * @param series the monthly trend line with month-over-month deltas
 * @param byType the visit totals per pet type, most visited first
 * @param warnings data-quality notes computed as values (future-dated visits, gaps in the
 * month series) — reusing the same {@link Violation} type as form validation
 */
public value record VisitTrendsReport(Vector<TrendPoint> series, Vector<TypeCount> byType, Vector<Violation> warnings) {
}
