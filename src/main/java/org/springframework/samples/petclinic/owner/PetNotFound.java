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

import org.springframework.samples.petclinic.functional.NotFound;

/**
 * No {@link Pet} with the given {@link PetId} belongs to the owner with the given
 * {@link OwnerId}. Carried as a value on the {@code Left} of an {@code Either} and rendered
 * as HTTP 404.
 */
public value record PetNotFound(OwnerId ownerId, PetId petId) implements NotFound {

	@Override
	public String message() {
		return "Pet not found with id: " + petId.id() + " for owner with id: " + ownerId.id();
	}

}
