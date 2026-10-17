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

import java.time.LocalDate;

import dev.zazr.control.Option;

/**
 * One pet's line on an owner report. {@code age} and {@code lastVisit} are
 * {@link Option}s: a pet with no birth date on file, or one that has never been seen, is
 * represented by {@link Option#none()} (JSON {@code null}) rather than a fabricated value.
 *
 * @param name the pet's name
 * @param type the pet's type (e.g. {@code dog})
 * @param age the pet's age in whole years, or empty when the birth date is unknown
 * @param lastVisit the date of the most recent visit, or empty when never visited
 * @param visits how many visits are on record
 */
public value record PetSummary(String name, String type, Option<Integer> age, Option<LocalDate> lastVisit, int visits) {
}
