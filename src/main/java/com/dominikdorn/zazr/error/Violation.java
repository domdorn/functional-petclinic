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

/**
 * A single validation failure as a value: which {@code field} failed, a stable
 * {@code code} (reused as the Spring error code so message resolution keeps working), and a
 * default human-readable {@code message}. An empty {@code field} denotes a global
 * (object-level) error.
 */
public value record Violation(String field, String code, String message) {

	/**
	 * A field-level violation.
	 * @param field the rejected field
	 * @param code the error code
	 * @param message the default message
	 * @return the violation
	 */
	public static Violation of(String field, String code, String message) {
		return new Violation(field, code, message);
	}

	/**
	 * A global (object-level) violation, not tied to a single field.
	 * @param code the error code
	 * @param message the default message
	 * @return the violation
	 */
	public static Violation global(String code, String message) {
		return new Violation("", code, message);
	}

	public boolean isGlobal() {
		return this.field.isEmpty();
	}

}
