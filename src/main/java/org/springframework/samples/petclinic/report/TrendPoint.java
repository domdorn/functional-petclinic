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

import dev.zazr.control.Option;

/**
 * One month on the visit-trend line: how many visits, and how that compares to the
 * previous month. The first month has no predecessor, so {@code delta} and
 * {@code changePct} are {@link Option#none()} rather than a fabricated zero — the absence
 * is modelled, not hidden. {@code changePct} is also absent when the previous month had
 * zero visits (no meaningful percentage).
 *
 * @param month the calendar month formatted as {@code yyyy-MM}
 * @param visits the number of visits that month
 * @param delta visits minus the previous month's visits, or empty for the first month
 * @param changePct the percentage change versus the previous month, or empty when there is
 * no comparable previous month
 */
public value record TrendPoint(String month, long visits, Option<Long> delta, Option<Double> changePct) {
}
