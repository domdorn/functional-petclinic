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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import com.dominikdorn.zazr.error.Violation;

import dev.zazr.control.Validation;
import dev.zazr.control.Validation.Invalid;

/**
 * Unit tests for {@link PetValidations}, showing independent rules accumulating into one
 * {@link Validation} value.
 */
class PetValidationsTests {

	private Owner ownerWithPet(String existingPetName) {
		Owner owner = new Owner();
		owner.setId(new OwnerId(1));
		Pet existing = new Pet();
		existing.setId(new PetId(1));
		existing.setName(existingPetName);
		owner.addPet(existing);
		return owner;
	}

	private List<Violation> violations(Validation<Violation, Pet> result) {
		List<Violation> collected = new ArrayList<>();
		((Invalid<Violation, Pet>) result).errors().forEach(collected::add);
		return collected;
	}

	@Test
	void newPetWithFreshNameAndPastBirthDateIsValid() {
		Owner owner = ownerWithPet("Leo");
		Pet pet = new Pet();
		pet.setName("Milo");
		pet.setBirthDate(LocalDate.now().minusDays(1));

		assertThat(PetValidations.forCreation(owner, pet).isValid()).isTrue();
	}

	@Test
	void creationAccumulatesDuplicateNameAndFutureBirthDate() {
		Owner owner = ownerWithPet("Leo");
		Pet pet = new Pet();
		pet.setName("Leo"); // duplicate of the existing pet
		pet.setBirthDate(LocalDate.now().plusDays(1)); // future

		Validation<Violation, Pet> result = PetValidations.forCreation(owner, pet);

		assertThat(result.isInvalid()).isTrue();
		assertThat(violations(result)).extracting(Violation::field).containsExactlyInAnyOrder("name", "birthDate");
	}

	@Test
	void updateAllowsKeepingOwnName() {
		Owner owner = ownerWithPet("Leo");
		Pet edited = owner.getPet("Leo"); // same persisted pet, same name
		edited.setBirthDate(LocalDate.now().minusYears(1));

		assertThat(PetValidations.forUpdate(owner, edited).isValid()).isTrue();
	}

}
