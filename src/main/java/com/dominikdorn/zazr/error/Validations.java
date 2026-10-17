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

import org.springframework.validation.Errors;

import dev.zazr.control.Either;
import dev.zazr.control.Validation;
import dev.zazr.control.Validation.Invalid;
import dev.zazr.control.Validation.Valid;

/**
 * Bridges a zazr {@link Validation} (validation as accumulated values) to the two ways
 * this application surfaces failures: Spring's {@link Errors}/{@code BindingResult} for
 * server-rendered forms, and an {@link Either} of {@link ValidationFailed} for the
 * {@code Either}-returning controllers.
 */
public final class Validations {

	private Validations() {
	}

	/**
	 * Push every {@link Violation} of an {@code Invalid} result into {@code errors} so a
	 * Thymeleaf form re-renders them; a {@code Valid} result adds nothing. The
	 * {@link Violation#code()} is reused as the Spring error code so message resolution
	 * is unchanged.
	 * @param validation the validation outcome
	 * @param errors the binding result to reject into
	 */
	public static void rejectInto(Validation<Violation, ?> validation, Errors errors) {
		switch (validation) {
			case Valid<Violation, ?> ignored -> {
			}
			case Invalid<Violation, ?> invalid -> {
				for (Violation violation : invalid.errors()) {
					if (violation.isGlobal()) {
						errors.reject(violation.code(), violation.message());
					}
					else {
						errors.rejectValue(violation.field(), violation.code(), violation.message());
					}
				}
			}
		}
	}

	/**
	 * Turn a validation outcome into an {@link Either}: the valid value on the right, or
	 * a {@link ValidationFailed} carrying every violation on the left.
	 * @param validation the validation outcome
	 * @param <T> the validated type
	 * @return the corresponding {@link Either}
	 */
	public static <T> Either<DomainError, T> toEither(Validation<Violation, T> validation) {
		return switch (validation) {
			case Valid<Violation, T> valid -> Either.right(valid.value());
			case Invalid<Violation, T> invalid -> Either.left(new ValidationFailed(invalid.errors()));
		};
	}

}
