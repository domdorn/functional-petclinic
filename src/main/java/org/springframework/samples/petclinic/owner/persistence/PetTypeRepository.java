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

package org.springframework.samples.petclinic.owner.persistence;

import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetTypeId;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dev.zazr.collection.Vector;

/**
 * Repository class for <code>PetType</code> domain objects.
 *
 * @author Patrick Baumgartner
 */

public interface PetTypeRepository extends JpaRepository<PetType, PetTypeId> {

	/**
	 * Spring Data query that materializes the rows; prefer {@link #findPetTypes()} which
	 * exposes the result as an immutable zazr {@link Vector}.
	 * @return the pet types as a java list
	 */
	@Query("SELECT ptype FROM PetType ptype ORDER BY ptype.name")
	List<PetType> findPetTypesList();

	/**
	 * Retrieve all {@link PetType}s from the data store.
	 * @return an immutable {@link Vector} of {@link PetType}s, ordered by name
	 */
	default Vector<PetType> findPetTypes() {
		return Vector.ofAll(findPetTypesList());
	}

}
