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
package org.springframework.samples.petclinic.owner.web;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerId;
import org.springframework.samples.petclinic.owner.OwnerNotFound;
import org.springframework.samples.petclinic.owner.persistence.OwnerRepository;

import java.util.List;
import java.util.Objects;

import org.springframework.samples.petclinic.functional.DomainError;
import org.springframework.samples.petclinic.report.OwnerReport;
import org.springframework.samples.petclinic.report.OwnerReportService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import jakarta.validation.Valid;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.zazr.control.Either;
import dev.zazr.control.Option;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Wick Dynex
 */
@Controller
public class OwnerController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

	private final OwnerRepository owners;

	private final OwnerReportService ownerReports;

	public OwnerController(OwnerRepository owners, OwnerReportService ownerReports) {
		this.owners = owners;
		this.ownerReports = ownerReports;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id", "*.id");
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Option<OwnerId> ownerId) {
		// Pure form-backing object: a missing owner is not an error here, it is an empty
		// form. Handlers that require the owner to exist (e.g. showOwner) enforce that as
		// a value via Either instead of throwing from this shared @ModelAttribute.
		// OptionPathVariableArgumentResolver guarantees a non-null Option (none when the
		// {ownerId} segment is absent).
		return ownerId.flatMap(this.owners::find).getOrElse(Owner::new);
	}

	@GetMapping("/owners/new")
	public String initCreationForm() {
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/owners/new")
	public String processCreationForm(@Valid Owner owner, BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in creating the owner.");
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "New Owner Created");
		return "redirect:/owners/" + owner.ownerId().map(id -> Integer.toString(id.id())).getOrElse("");
	}

	@GetMapping("/owners/find")
	public String initFindForm() {
		return "owners/findOwners";
	}

	@GetMapping("/owners")
	public String processFindForm(@RequestParam(defaultValue = "1") int page, Owner owner, BindingResult result,
			Model model, RedirectAttributes redirectAttributes) {
		// allow parameterless GET request for /owners to return all records
		String lastName = owner.getLastName();
		if (lastName == null) {
			lastName = ""; // empty string signifies broadest possible search
		}
		else {
			lastName = lastName.strip();
		}

		// find owners by last name
		Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, lastName);
		if (page < 1 || page > Math.max(ownersResults.getTotalPages(), 1)) {
			redirectAttributes.addAttribute("page", 1);
			if (!lastName.isEmpty()) {
				redirectAttributes.addAttribute("lastName", lastName);
			}
			return "redirect:/owners";
		}

		if (ownersResults.isEmpty()) {
			// no owners found
			result.rejectValue("lastName", "notFound", "not found");
			return "owners/findOwners";
		}

		if (ownersResults.getTotalElements() == 1) {
			// 1 owner found
			owner = ownersResults.iterator().next();
			return "redirect:/owners/" + owner.ownerId().map(id -> Integer.toString(id.id())).getOrElse("");
		}

		// multiple owners found
		return addPaginationModel(page, model, ownersResults);
	}

	private String addPaginationModel(int page, Model model, Page<Owner> paginated) {
		List<Owner> listOwners = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listOwners", listOwners);
		return "owners/ownersList";
	}

	private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {
		int pageSize = 5;
		int validatedPage = Math.max(page, 1);
		Pageable pageable = PageRequest.of(validatedPage - 1, pageSize);
		return owners.findByLastNameStartingWith(lastname, pageable);
	}

	@GetMapping("/owners/{ownerId}/edit")
	public String initUpdateOwnerForm() {
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/owners/{ownerId}/edit")
	public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result,
			@PathVariable("ownerId") OwnerId ownerId, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in updating the owner.");
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		if (!Objects.equals(owner.getId(), ownerId)) {
			result.rejectValue("id", "mismatch", "The owner ID in the form does not match the URL.");
			redirectAttributes.addFlashAttribute("error", "Owner ID mismatch. Please try again.");
			return "redirect:/owners/{ownerId}/edit";
		}

		owner.setId(ownerId);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "Owner Values Updated");
		return "redirect:/owners/{ownerId}";
	}

	/**
	 * Custom handler for displaying an owner.
	 * @param ownerId the ID of the owner to display
	 * @return the owner details view on the right, or an {@link OwnerNotFound} error on
	 * the left when no such owner exists
	 */
	@GetMapping("/owners/{ownerId}")
	public Either<DomainError, ModelAndView> showOwner(@PathVariable("ownerId") int ownerId) {
		OwnerId id = new OwnerId(ownerId);
		return this.owners.find(id)
			.<DomainError>toEither(() -> new OwnerNotFound(id))
			.map(owner -> new ModelAndView("owners/ownerDetails").addObject(owner));
	}

	/**
	 * JSON variant of {@link #showOwner}: the same {@code Either} value rendered as a
	 * body. {@code Right} is the owner as JSON (200); {@code Left} is the error as JSON
	 * with its mapped status (404).
	 * @param ownerId the owner id
	 * @return the owner on the right, or an {@link OwnerNotFound} on the left
	 */
	@GetMapping(path = "/owners/{ownerId}/summary", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Either<DomainError, Owner> ownerSummary(@PathVariable("ownerId") int ownerId) {
		OwnerId id = new OwnerId(ownerId);
		return this.owners.find(id).<DomainError>toEither(() -> new OwnerNotFound(id));
	}

	/**
	 * Per-owner care report as JSON: a summary line per pet plus accumulated data-quality
	 * warnings. {@code Right} is the report (200); {@code Left} is an
	 * {@link OwnerNotFound} rendered as an RFC-7807 problem (404).
	 * @param ownerId the owner id
	 * @return the report on the right, or an {@link OwnerNotFound} on the left
	 */
	@GetMapping(path = "/owners/{ownerId}/report", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Either<DomainError, OwnerReport> ownerReport(@PathVariable("ownerId") int ownerId) {
		OwnerId id = new OwnerId(ownerId);
		return this.owners.find(id).<DomainError>toEither(() -> new OwnerNotFound(id)).map(this.ownerReports::build);
	}

}
