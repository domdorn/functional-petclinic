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

import org.junit.jupiter.api.Test;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.Visit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Drives the pure {@link OwnerReportService#report(Owner, LocalDate)} core with in-memory
 * owners: ages and last-visit come back as {@link dev.zazr.control.Option}s, and the
 * warnings strip shows {@link dev.zazr.control.Validation} accumulating every
 * data-quality issue at once.
 */
class OwnerReportServiceTests {

	private static final LocalDate TODAY = LocalDate.parse("2026-10-17");

	private final OwnerReportService service = new OwnerReportService(Clock.systemUTC());

	private static Pet pet(String name, String type, LocalDate birthDate, LocalDate... visitDates) {
		Pet pet = new Pet();
		pet.setName(name);
		PetType petType = new PetType();
		petType.setName(type);
		pet.setType(petType);
		if (birthDate != null) {
			pet.setBirthDate(birthDate);
		}
		for (LocalDate date : visitDates) {
			Visit visit = new Visit();
			visit.setDate(date);
			visit.setDescription("checkup");
			pet.addVisit(visit);
		}
		return pet;
	}

	private static Owner owner(Pet... pets) {
		Owner owner = new Owner();
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		for (Pet pet : pets) {
			owner.addPet(pet);
		}
		return owner;
	}

	@Test
	void summarizesAgeAndLastVisitWhenDataIsComplete() {
		Owner owner = owner(pet("Leo", "cat", LocalDate.parse("2020-09-07"), LocalDate.parse("2026-03-05"),
				LocalDate.parse("2026-08-20")));

		OwnerReport report = this.service.report(owner, TODAY);
		PetSummary leo = report.pets().get(0);

		assertThat(report.owner()).isEqualTo("George Franklin");
		assertThat(leo.age().get()).isEqualTo(6);
		assertThat(leo.lastVisit().get()).isEqualTo(LocalDate.parse("2026-08-20"));
		assertThat(leo.visits()).isEqualTo(2);
		assertThat(report.warnings().isEmpty()).isTrue();
	}

	@Test
	void missingBirthDateAndNoVisitsBecomeAbsentOptionsNotZeros() {
		Owner owner = owner(pet("Ghost", "dog", null));

		OwnerReport report = this.service.report(owner, TODAY);
		PetSummary ghost = report.pets().get(0);

		assertThat(ghost.age().isDefined()).isFalse();
		assertThat(ghost.lastVisit().isDefined()).isFalse();
		assertThat(ghost.visits()).isZero();
	}

	@Test
	void warningsAccumulateEveryIssueForAPet() {
		// No birth date AND never seen: Validation should report BOTH, not just the
		// first.
		Owner owner = owner(pet("Ghost", "dog", null));

		OwnerReport report = this.service.report(owner, TODAY);

		assertThat(report.warnings().map(v -> v.message()).asJava()).anyMatch(m -> m.contains("no birth date"))
			.anyMatch(m -> m.contains("never been seen"));
		assertThat(report.warnings().size()).isEqualTo(2);
	}

	@Test
	void warnsAboutFutureDatedVisit() {
		Owner owner = owner(pet("Leo", "cat", LocalDate.parse("2020-09-07"), LocalDate.parse("2026-12-01")));

		OwnerReport report = this.service.report(owner, TODAY);

		assertThat(report.warnings().map(v -> v.message()).asJava()).anyMatch(m -> m.contains("future"));
	}

}
