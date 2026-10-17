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
package org.springframework.samples.petclinic.vet;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Identity of a {@link Specialty}, as a typed value instead of a bare {@code int}.
 */
public value record SpecialtyId(int id) implements Serializable {

	@JsonValue
	public int id() {
		return this.id;
	}

	/**
	 * The plain numeric id, so Thymeleaf URL expressions (e.g. {@code ${owner.id}})
	 * and logging render it as the number rather than the record's default form.
	 */
	@Override
	public String toString() {
		return Integer.toString(this.id);
	}

}
