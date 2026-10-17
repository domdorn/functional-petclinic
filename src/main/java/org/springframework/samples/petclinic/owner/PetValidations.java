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
import java.util.Objects;

import org.springframework.samples.petclinic.functional.Violation;
import org.springframework.util.StringUtils;

import dev.zazr.control.Validation;

/**
 * Business validation for a {@link Pet} within its owner, expressed as accumulated
 * values. The independent rules (name uniqueness, birth date not in the future) are
 * combined with {@link Validation#zipWith} so <em>every</em> violation is reported at
 * once. Field presence (a non-blank name, a chosen type) stays with the Bean/Spring
 * validator on the form.
 */
public final class PetValidations {

	private PetValidations() {
	}

	/**
	 * Rules for adding a new pet to an owner.
	 * @param owner the owning owner
	 * @param pet the pet being created
	 * @return the accumulated validation
	 */
	public static Validation<Violation, Pet> forCreation(Owner owner, Pet pet) {
		return nameUniqueForCreation(owner, pet).zipWith(birthDateNotInFuture(pet), (first, second) -> pet);
	}

	/**
	 * Rules for editing an existing pet of an owner.
	 * @param owner the owning owner
	 * @param pet the pet being updated
	 * @return the accumulated validation
	 */
	public static Validation<Violation, Pet> forUpdate(Owner owner, Pet pet) {
		return nameUniqueForUpdate(owner, pet).zipWith(birthDateNotInFuture(pet), (first, second) -> pet);
	}

	private static Validation<Violation, Pet> nameUniqueForCreation(Owner owner, Pet pet) {
		boolean duplicate = StringUtils.hasText(pet.getName()) && pet.isNew()
				&& owner.getPet(pet.getName(), true) != null;
		return duplicate ? Validation.invalid(Violation.of("name", "duplicate", "already exists"))
				: Validation.valid(pet);
	}

	private static Validation<Violation, Pet> nameUniqueForUpdate(Owner owner, Pet pet) {
		boolean duplicate = false;
		if (StringUtils.hasText(pet.getName())) {
			Pet existing = owner.getPet(pet.getName(), false);
			duplicate = existing != null && !Objects.equals(existing.getId(), pet.getId());
		}
		return duplicate ? Validation.invalid(Violation.of("name", "duplicate", "already exists"))
				: Validation.valid(pet);
	}

	private static Validation<Violation, Pet> birthDateNotInFuture(Pet pet) {
		boolean future = pet.birthDate().exists(date -> date.isAfter(LocalDate.now()));
		return future
				? Validation.invalid(Violation.of("birthDate", "typeMismatch.birthDate", "must not be in the future"))
				: Validation.valid(pet);
	}

}
