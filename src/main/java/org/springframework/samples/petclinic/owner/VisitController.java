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
import java.util.Map;

import org.springframework.samples.petclinic.functional.DomainError;
import org.springframework.samples.petclinic.functional.Validations;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.zazr.control.Either;
import dev.zazr.control.Option;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 * @author Wick Dynex
 */
@Controller
class VisitController {

	private final OwnerRepository owners;

	public VisitController(OwnerRepository owners) {
		this.owners = owners;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id", "*.id");
	}

	/**
	 * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure
	 * we always have fresh data - Since we do not use the session scope, make sure that
	 * Pet object always has an id (Even though id is not part of the form fields)
	 * @param petId
	 * @return Pet
	 */
	@ModelAttribute("visit")
	public Visit loadPetWithVisit(@PathVariable("ownerId") OwnerId ownerId, @PathVariable("petId") PetId petId,
			Map<String, Object> model) {
		// Non-throwing form-backing object; existence is enforced as a value by the
		// handlers via requireOwnerPet.
		Visit visit = new Visit();
		this.owners.find(ownerId).forEach(owner -> {
			model.put("owner", owner);
			Option.ofNullable(owner.getPet(petId.id())).forEach(pet -> {
				model.put("pet", pet);
				pet.addVisit(visit);
			});
		});
		return visit;
	}

	private Either<DomainError, Pet> requireOwnerPet(OwnerId ownerId, PetId petId) {
		return this.owners.find(ownerId)
			.<DomainError>toEither(() -> new OwnerNotFound(ownerId))
			.flatMap(owner -> Option.ofNullable(owner.getPet(petId.id()))
				.<DomainError>toEither(() -> new PetNotFound(ownerId, petId)));
	}

	@ModelAttribute("minVisitDate")
	public LocalDate minVisitDate() {
		return LocalDate.now().plusDays(1);
	}

	// Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is
	// called
	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public Either<DomainError, String> initNewVisitForm(@PathVariable("ownerId") OwnerId ownerId,
			@PathVariable("petId") PetId petId) {
		return requireOwnerPet(ownerId, petId).map(ignored -> "pets/createOrUpdateVisitForm");
	}

	// Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is
	// called
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public Either<DomainError, String> processNewVisitForm(@PathVariable("ownerId") OwnerId ownerId,
			@ModelAttribute Owner owner, @PathVariable("petId") PetId petId, @Valid Visit visit, BindingResult result,
			RedirectAttributes redirectAttributes) {
		return requireOwnerPet(ownerId, petId).map(ignored -> {
			Validations.rejectInto(VisitValidator.validate(visit), result);

			if (result.hasErrors()) {
				return "pets/createOrUpdateVisitForm";
			}

			owner.addVisit(petId.id(), visit);
			this.owners.save(owner);
			redirectAttributes.addFlashAttribute("message", "Your visit has been booked");
			return "redirect:/owners/{ownerId}";
		});
	}

}
