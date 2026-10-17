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
package org.springframework.samples.petclinic.report;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.functional.Violation;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.stereotype.Service;

import dev.zazr.collection.NonEmptyVector;
import dev.zazr.collection.Vector;
import dev.zazr.control.Option;
import dev.zazr.control.Validation;

/**
 * Builds a per-owner care report. Age and last-visit are modelled as {@link Option}s so
 * missing data is explicit rather than faked. The warnings strip is the
 * {@link Validation} showcase: each pet's independent data-quality checks are combined
 * with {@code zipWith}, which <em>accumulates</em> every violation at once — unlike
 * {@code Either}, which would stop at the first.
 */
@Service
public class OwnerReportService {

	private final Clock clock;

	public OwnerReportService(Clock clock) {
		this.clock = clock;
	}

	public OwnerReport build(Owner owner) {
		return report(owner, LocalDate.now(this.clock));
	}

	/**
	 * The pure core: an owner and a reference "today" in, report out. Package-visible so
	 * tests can drive it with a fixed date.
	 * @param owner the owner to report on
	 * @param today the date used for ages and "future visit" checks
	 * @return the assembled report
	 */
	OwnerReport report(Owner owner, LocalDate today) {
		Vector<PetSummary> pets = owner.getPets().map(pet -> summarize(pet, today));
		Vector<Violation> warnings = owner.getPets().flatMap(pet -> dataQuality(pet, today));
		return new OwnerReport(owner.getFirstName() + " " + owner.getLastName(), pets, warnings);
	}

	private PetSummary summarize(Pet pet, LocalDate today) {
		Option<Integer> age = pet.birthDate().map(birth -> (int) ChronoUnit.YEARS.between(birth, today));
		Option<LocalDate> lastVisit = pet.getVisits().map(Visit::getDate).max();
		return new PetSummary(pet.getName(), pet.getType().getName(), age, lastVisit, pet.getVisits().size());
	}

	private Vector<Violation> dataQuality(Pet pet, LocalDate today) {
		Validation<Violation, Pet> hasBirthDate = Validation.fromPredicate(pet, p -> p.birthDate().isDefined(),
				p -> Violation.of(p.getName() + ".birthDate", "missing", p.getName() + " has no birth date on file"));
		Validation<Violation, Pet> hasBeenSeen = Validation.fromPredicate(pet, p -> !p.getVisits().isEmpty(),
				p -> Violation.of(p.getName() + ".visits", "none", p.getName() + " has never been seen"));
		Validation<Violation, Pet> noFutureVisit = Validation.fromPredicate(pet,
				p -> p.getVisits().map(Visit::getDate).filter(date -> date.isAfter(today)).isEmpty(),
				p -> Violation.of(p.getName() + ".visits", "future", p.getName() + " has a visit dated in the future"));

		// zipWith accumulates all three checks' violations; it does not short-circuit.
		Validation<Violation, Pet> checked = Validation.zipWith(hasBirthDate, hasBeenSeen, noFutureVisit,
				(a, b, c) -> pet);
		return checked.fold(NonEmptyVector::toVector, ok -> Vector.empty());
	}

}
