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

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import dev.zazr.control.Option;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OwnerTests {

	@Test
	void addPetAddsPersistedPet() {
		Owner owner = new Owner();
		Pet pet = new Pet();
		pet.setId(new PetId(5));
		pet.setName("Buddy");

		owner.addPet(pet);

		assertTrue(owner.getPets().contains(pet));
		assertEquals(1, owner.getPets().size());
	}

	@Test
	void typedIdIsPresentWhenPersisted() {
		Owner owner = new Owner();
		owner.setId(new OwnerId(7));
		Pet pet = new Pet();
		pet.setId(new PetId(3));
		Visit visit = new Visit();
		visit.setId(new VisitId(9));

		assertThat(owner.ownerId()).isEqualTo(Option.some(new OwnerId(7)));
		assertThat(pet.petId()).isEqualTo(Option.some(new PetId(3)));
		assertThat(visit.visitId()).isEqualTo(Option.some(new VisitId(9)));
	}

	@Test
	void typedIdIsEmptyWhenTransient() {
		assertThat(new Owner().ownerId()).isEqualTo(Option.none());
		assertThat(new Pet().petId()).isEqualTo(Option.none());
		assertThat(new Visit().visitId()).isEqualTo(Option.none());
	}

	@Test
	void petBirthDateIsOptional() {
		Pet pet = new Pet();
		assertThat(pet.birthDate()).isEqualTo(Option.none());

		LocalDate date = LocalDate.of(2020, 1, 2);
		pet.setBirthDate(date);
		assertThat(pet.birthDate()).isEqualTo(Option.some(date));
		assertThat(pet.getBirthDate()).isEqualTo(date); // nullable facade for form
														// binding

		pet.setBirthDate(null);
		assertThat(pet.birthDate().isEmpty()).isTrue();
		assertThat(pet.getBirthDate()).isNull();
	}

	@Test
	void addPetDoesNotAddDuplicatePet() {
		Owner owner = new Owner();
		Pet pet = new Pet();
		pet.setId(new PetId(5));
		pet.setName("Buddy");

		owner.addPet(pet);
		owner.addPet(pet);

		assertEquals(1, owner.getPets().size());
	}

}
