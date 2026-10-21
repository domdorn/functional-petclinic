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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The language-feature demo from the talk, as runnable proof: a plain {@code record} is an
 * identity object, so two value-equal instances are <em>not</em> {@code ==}; a
 * {@code value record} has no identity, so {@code ==} <em>is</em> value equality.
 *
 * <p>
 * Requires {@code --enable-preview} (configured in the build); value classes are a Java 28
 * preview feature.
 */
class ValueRecordIdentityTests {

	/** Ordinary identity record. */
	record IdentityPoint(int x, int y) {
	}

	/** Same shape, but a value record — no identity. */
	value record ValuePoint(int x, int y) {
	}

	@Test
	void plainRecordIsValueEqualButNotIdentical() {
		IdentityPoint a = new IdentityPoint(1, 2);
		IdentityPoint b = new IdentityPoint(1, 2);

		assertThat(a).isEqualTo(b); // equals(): true
		assertThat(a == b).isFalse(); // two distinct objects on the heap
	}

	@Test
	void valueRecordIsIdenticalWhenValueEqual() {
		ValuePoint a = new ValuePoint(1, 2);
		ValuePoint b = new ValuePoint(1, 2);

		assertThat(a).isEqualTo(b); // equals(): true
		assertThat(a == b).isTrue(); // no identity to distinguish them
	}

}
