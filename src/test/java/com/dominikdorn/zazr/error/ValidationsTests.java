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
package com.dominikdorn.zazr.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import dev.zazr.control.Either;
import dev.zazr.control.Validation;

/**
 * Tests the bridge from a zazr {@link Validation} to Spring's {@link Errors} and to an
 * {@link Either} of {@link DomainError}.
 */
class ValidationsTests {

	@Test
	void rejectIntoAddsFieldErrorForInvalid() {
		Validation<Violation, String> invalid = Validation
			.invalid(Violation.of("date", "typeMismatch.visitDate", "bad"));
		Errors errors = new MapBindingResult(new HashMap<>(), "visit");

		Validations.rejectInto(invalid, errors);

		assertThat(errors.hasFieldErrors("date")).isTrue();
		assertThat(errors.getFieldError("date").getCode()).isEqualTo("typeMismatch.visitDate");
	}

	@Test
	void rejectIntoAddsNothingForValid() {
		Validation<Violation, String> valid = Validation.valid("ok");
		Errors errors = new MapBindingResult(new HashMap<>(), "visit");

		Validations.rejectInto(valid, errors);

		assertThat(errors.hasErrors()).isFalse();
	}

	@Test
	void toEitherMapsInvalidToValidationFailed() {
		Validation<Violation, String> invalid = Validation.invalid(Violation.global("bad", "boom"));

		Either<DomainError, String> either = Validations.toEither(invalid);

		assertThat(either.isLeft()).isTrue();
		DomainError error = either.getLeft();
		assertThat(error).isInstanceOf(ValidationFailed.class);
		assertThat(error.message()).isEqualTo("boom");
	}

	@Test
	void toEitherMapsValidToRight() {
		Either<DomainError, String> either = Validations.toEither(Validation.valid("ok"));
		assertThat(either.isRight()).isTrue();
		assertThat(either.getOrNull()).isEqualTo("ok");
	}

}
