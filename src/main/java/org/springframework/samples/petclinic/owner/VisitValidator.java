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
package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;

import com.dominikdorn.zazr.error.Violation;

import dev.zazr.control.Validation;

/**
 * Business validation for a {@link Visit} expressed as a value: the result is a
 * {@link Validation} that is either the valid visit or the accumulated
 * {@link Violation}s, rather than a side effect on a {@code BindingResult}. Field-level
 * constraints (a non-blank, bounded description) stay with Bean Validation on the entity.
 */
public final class VisitValidator {

	private VisitValidator() {
	}

	/**
	 * A visit's date, when given, must be in the future.
	 * @param visit the visit to check
	 * @return {@code Valid(visit)} or {@code Invalid} with a {@code date} violation
	 */
	public static Validation<Violation, Visit> validate(Visit visit) {
		return Validation.fromPredicate(visit, v -> v.getDate() == null || v.getDate().isAfter(LocalDate.now()),
				v -> Violation.of("date", "typeMismatch.visitDate", "must be a future date"));
	}

}
