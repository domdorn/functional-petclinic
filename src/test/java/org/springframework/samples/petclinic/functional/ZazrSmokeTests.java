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
package org.springframework.samples.petclinic.functional;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import dev.zazr.control.Either;
import dev.zazr.control.Either.Left;
import dev.zazr.control.Either.Right;
import dev.zazr.control.Option;
import dev.zazr.control.Validation;

/**
 * Smoke test proving the Zazr functional library resolves from Maven Central and runs on
 * the project's JDK 28 (preview) toolchain. Exercises pattern matching over Zazr's sealed
 * control types, the foundation for errors-as-values in this application.
 */
class ZazrSmokeTests {

	@Test
	void eitherIsPatternMatchable() {
		Either<String, Integer> right = Either.right(42);
		String label = switch (right) {
			case Right(var value) -> "ok: " + value;
			case Left(var error) -> "err: " + error;
		};
		assertThat(label).isEqualTo("ok: 42");
	}

	@Test
	void optionWrapsNullableAtTheBoundary() {
		assertThat(Option.ofNullable(null).isEmpty()).isTrue();
		assertThat(Option.ofNullable("x").getOrElse("fallback")).isEqualTo("x");
	}

	@Test
	void validationAccumulatesEveryError() {
		Validation<String, String> blankName = Validation.invalid("name is required");
		Validation<String, Integer> badAge = Validation.invalid("age must be positive");

		Validation<String, String> combined = Validation.zipWith(blankName, badAge, (name, age) -> name + age);

		String message = combined.fold(errors -> errors.mkString("; "), ok -> ok);
		assertThat(message).isEqualTo("name is required; age must be positive");
	}

}
