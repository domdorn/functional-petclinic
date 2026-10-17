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

import java.util.Collection;

import org.springframework.dao.DataIntegrityViolationException;
import com.dominikdorn.zazr.error.DomainError;
import com.dominikdorn.zazr.error.Validations;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.zazr.control.Either;
import dev.zazr.control.Option;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Wick Dynex
 */
@Controller
@RequestMapping("/owners/{ownerId}")
class PetController {

	private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";

	private final OwnerRepository owners;

	private final PetTypeRepository types;

	public PetController(OwnerRepository owners, PetTypeRepository types) {
		this.owners = owners;
		this.types = types;
	}

	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		// Expose a plain java collection to the view layer; the repository speaks zazr.
		return this.types.findPetTypes().asJava();
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable("ownerId") OwnerId ownerId) {
		// Non-throwing form-backing object; existence is enforced as a value by the
		// handlers via requireOwner.
		return this.owners.find(ownerId).getOrElse(Owner::new);
	}

	@ModelAttribute("pet")
	public Pet findPet(@PathVariable("ownerId") OwnerId ownerId,
			@PathVariable(name = "petId", required = false) Option<PetId> petId) {
		return petId.flatMap(id -> this.owners.find(ownerId).flatMap(owner -> Option.ofNullable(owner.getPet(id.id()))))
			.getOrElse(Pet::new);
	}

	private Either<DomainError, Owner> requireOwner(OwnerId ownerId) {
		return this.owners.find(ownerId).<DomainError>toEither(() -> new OwnerNotFound(ownerId));
	}

	private Either<DomainError, Pet> requirePet(Owner owner, OwnerId ownerId, PetId petId) {
		return Option.ofNullable(owner.getPet(petId.id())).<DomainError>toEither(() -> new PetNotFound(ownerId, petId));
	}

	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id", "*.id");
	}

	@InitBinder("pet")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new PetValidator());
		dataBinder.setDisallowedFields("id", "*.id");
	}

	@GetMapping("/pets/new")
	public Either<DomainError, String> initCreationForm(@PathVariable("ownerId") OwnerId ownerId, Owner owner) {
		return requireOwner(ownerId).map(ignored -> {
			Pet pet = new Pet();
			owner.addPet(pet);
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		});
	}

	@PostMapping("/pets/new")
	public Either<DomainError, String> processCreationForm(@PathVariable("ownerId") OwnerId ownerId, Owner owner,
			@Valid Pet pet, BindingResult result, RedirectAttributes redirectAttributes) {
		return requireOwner(ownerId).map(ignored -> {
			Validations.rejectInto(PetValidations.forCreation(owner, pet), result);

			if (result.hasErrors()) {
				return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
			}

			try {
				owner.addPet(pet);
				this.owners.saveAndFlush(owner);
			}
			catch (DataIntegrityViolationException ex) {
				if (!isDuplicatePetNameViolation(ex)) {
					throw ex;
				}
				result.rejectValue("name", "duplicate", "already exists");
				return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
			}
			redirectAttributes.addFlashAttribute("message", "New Pet has been Added");
			return "redirect:/owners/{ownerId}";
		});
	}

	@GetMapping("/pets/{petId}/edit")
	public Either<DomainError, String> initUpdateForm(@PathVariable("ownerId") OwnerId ownerId,
			@PathVariable("petId") PetId petId) {
		return requireOwner(ownerId).flatMap(owner -> requirePet(owner, ownerId, petId))
			.map(ignored -> VIEWS_PETS_CREATE_OR_UPDATE_FORM);
	}

	@PostMapping("/pets/{petId}/edit")
	public Either<DomainError, String> processUpdateForm(@PathVariable("ownerId") OwnerId ownerId, Owner owner,
			@Valid Pet pet, BindingResult result, RedirectAttributes redirectAttributes) {
		return requireOwner(ownerId).map(ignored -> {
			Validations.rejectInto(PetValidations.forUpdate(owner, pet), result);

			if (result.hasErrors()) {
				return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
			}

			try {
				updatePetDetails(owner, pet);
			}
			catch (DataIntegrityViolationException ex) {
				if (!isDuplicatePetNameViolation(ex)) {
					throw ex;
				}
				result.rejectValue("name", "duplicate", "already exists");
				return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
			}
			redirectAttributes.addFlashAttribute("message", "Pet details has been edited");
			return "redirect:/owners/{ownerId}";
		});
	}

	/**
	 * Updates the pet details if it exists or adds a new pet to the owner.
	 * @param owner The owner of the pet
	 * @param pet The pet with updated details
	 */
	private void updatePetDetails(Owner owner, Pet pet) {
		PetId id = pet.getId();
		Assert.state(id != null, "'pet.getId()' must not be null");
		Pet existingPet = owner.getPet(id.id());
		if (existingPet != null) {
			// Update existing pet's properties
			existingPet.setName(pet.getName());
			existingPet.setBirthDate(pet.getBirthDate());
			existingPet.setType(pet.getType());
		}
		else {
			owner.addPet(pet);
		}
		this.owners.saveAndFlush(owner);
	}

	private boolean isDuplicatePetNameViolation(DataIntegrityViolationException ex) {
		String message = ex.getMessage();
		return message != null && message.toLowerCase().contains("unique_owner_pet_name");
	}

}
