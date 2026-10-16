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
 * Identity of a {@link Vet}, as a typed value instead of a bare {@code int}. Being a
 * {@code value record} it is an immutable, identity-free carrier the JVM may flatten.
 */
public value record VetId(int id) implements Serializable {

	@JsonValue
	public int id() {
		return this.id;
	}

}
