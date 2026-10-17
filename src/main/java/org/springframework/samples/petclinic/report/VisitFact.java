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

/**
 * A single visit reduced to just what the reports need: when it happened and the pet type
 * it was for. This is a read model populated by a JPQL constructor expression in
 * {@link ReportRepository}; it is a plain {@code record} (not a {@code value record}) so
 * Hibernate can instantiate it reflectively.
 *
 * @param date the visit date
 * @param petType the name of the pet's type (e.g. {@code dog})
 */
public record VisitFact(LocalDate date, String petType) {
}
