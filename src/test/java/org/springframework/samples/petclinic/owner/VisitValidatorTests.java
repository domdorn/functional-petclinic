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

import org.springframework.samples.petclinic.owner.web.VisitValidator;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.functional.Violation;

import dev.zazr.control.Validation;
import dev.zazr.control.Validation.Invalid;

/**
 * Unit tests for {@link VisitValidator}: validation expressed as a value, with no Spring
 * machinery involved.
 */
class VisitValidatorTests {

	@Test
	void futureDateIsValid() {
		Visit visit = new Visit(); // defaults to tomorrow
		assertThat(VisitValidator.validate(visit).isValid()).isTrue();
	}

	@Test
	void todayIsInvalidWithDateViolation() {
		Visit visit = new Visit();
		visit.setDate(LocalDate.now());

		Validation<Violation, Visit> result = VisitValidator.validate(visit);

		assertThat(result.isInvalid()).isTrue();
		Violation violation = ((Invalid<Violation, Visit>) result).errors().head();
		assertThat(violation.field()).isEqualTo("date");
		assertThat(violation.code()).isEqualTo("typeMismatch.visitDate");
	}

}
