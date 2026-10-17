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

package org.springframework.samples.petclinic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalDate;

import dev.zazr.collection.Vector;
import dev.zazr.control.Option;
import org.springframework.samples.petclinic.owner.OwnerId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.persistence.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.persistence.PetTypeRepository;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.persistence.VetRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test of the Service and the Repository layer.
 * <p>
 * ClinicServiceSpringDataJpaTests subclasses benefit from the following services provided
 * by the Spring TestContext Framework:
 * </p>
 * <ul>
 * <li><strong>Spring IoC container caching</strong> which spares us unnecessary set up
 * time between test execution.</li>
 * <li><strong>Dependency Injection</strong> of test fixture instances, meaning that we
 * don't need to perform application context lookups. See the use of
 * {@link Autowired @Autowired} on the <code> </code> instance variable, which uses
 * autowiring <em>by type</em>.
 * <li><strong>Transaction management</strong>, meaning each test method is executed in
 * its own transaction, which is automatically rolled back by default. Thus, even if tests
 * insert or otherwise change database state, there is no need for a teardown or cleanup
 * script.
 * <li>An {@link org.springframework.context.ApplicationContext ApplicationContext} is
 * also inherited and can be used for explicit bean lookup if necessary.</li>
 * </ul>
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Dave Syer
 */
@DataJpaTest
// Ensure that if the mysql profile is active we connect to the real database:
@AutoConfigureTestDatabase(replace = Replace.NONE)
// @TestPropertySource("/application-postgres.properties")
class ClinicServiceTests {

	@Autowired
	protected OwnerRepository owners;

	@Autowired
	protected PetTypeRepository types;

	@Autowired
	protected VetRepository vets;

	private final Pageable pageable = Pageable.unpaged();

	@Test
	void shouldFindOwnersByLastName() {
		Page<Owner> owners = this.owners.findByLastNameStartingWith("Davis", pageable);
		assertThat(owners).hasSize(2);

		owners = this.owners.findByLastNameStartingWith("Daviss", pageable);
		assertThat(owners).isEmpty();
	}

	@Test
	void shouldFindSingleOwnerWithPet() {
		Option<Owner> optionalOwner = this.owners.find(new OwnerId(1));
		assertThat(optionalOwner.isDefined()).isTrue();
		Owner owner = optionalOwner.get();
		assertThat(owner.getLastName()).startsWith("Franklin");
		assertThat(owner.getPets()).hasSize(1);
		assertThat(owner.getPets().get(0).getType()).isNotNull();
		assertThat(owner.getPets().get(0).getType().getName()).isEqualTo("cat");
	}

	@Test
	@Transactional
	void shouldInsertOwner() {
		Page<Owner> owners = this.owners.findByLastNameStartingWith("Schultz", pageable);
		int found = (int) owners.getTotalElements();

		Owner owner = new Owner();
		owner.setFirstName("Sam");
		owner.setLastName("Schultz");
		owner.setAddress("4, Evans Street");
		owner.setCity("Wollongong");
		owner.setTelephone("4444444444");
		this.owners.save(owner);
		assertThat(owner.getId()).isNotNull();

		owners = this.owners.findByLastNameStartingWith("Schultz", pageable);
		assertThat(owners.getTotalElements()).isEqualTo(found + 1);
	}

	@Test
	@Transactional
	void shouldUpdateOwner() {
		Option<Owner> optionalOwner = this.owners.find(new OwnerId(1));
		assertThat(optionalOwner.isDefined()).isTrue();
		Owner owner = optionalOwner.get();
		String oldLastName = owner.getLastName();
		String newLastName = oldLastName + "X";

		owner.setLastName(newLastName);
		this.owners.save(owner);

		// retrieving new name from database
		optionalOwner = this.owners.find(new OwnerId(1));
		assertThat(optionalOwner.isDefined()).isTrue();
		owner = optionalOwner.get();
		assertThat(owner.getLastName()).isEqualTo(newLastName);
	}

	@Test
	void shouldFindAllPetTypes() {
		Vector<PetType> petTypes = this.types.findPetTypes();

		PetType petType1 = EntityUtils.getById(petTypes, PetType.class, 1, pt -> pt.getId().id());
		assertThat(petType1.getName()).isEqualTo("cat");
		PetType petType4 = EntityUtils.getById(petTypes, PetType.class, 4, pt -> pt.getId().id());
		assertThat(petType4.getName()).isEqualTo("snake");
	}

	@Test
	@Transactional
	void shouldInsertPetIntoDatabaseAndGenerateId() {
		Option<Owner> optionalOwner = this.owners.find(new OwnerId(6));
		assertThat(optionalOwner.isDefined()).isTrue();
		Owner owner6 = optionalOwner.get();

		int found = owner6.getPets().size();

		Pet pet = new Pet();
		pet.setName("bowser");
		Vector<PetType> types = this.types.findPetTypes();
		pet.setType(EntityUtils.getById(types, PetType.class, 2, pt -> pt.getId().id()));
		pet.setBirthDate(LocalDate.now());
		owner6.addPet(pet);
		assertThat(owner6.getPets()).hasSize(found + 1);

		this.owners.save(owner6);

		optionalOwner = this.owners.find(new OwnerId(6));
		assertThat(optionalOwner.isDefined()).isTrue();
		owner6 = optionalOwner.get();
		assertThat(owner6.getPets()).hasSize(found + 1);
		// checks that id has been generated
		pet = owner6.getPet("bowser");
		assertThat(pet.getId()).isNotNull();
	}

	@Test
	@Transactional
	void shouldUpdatePetName() {
		Option<Owner> optionalOwner = this.owners.find(new OwnerId(6));
		assertThat(optionalOwner.isDefined()).isTrue();
		Owner owner6 = optionalOwner.get();

		Pet pet7 = owner6.getPet(7);
		String oldName = pet7.getName();

		String newName = oldName + "X";
		pet7.setName(newName);
		this.owners.save(owner6);

		optionalOwner = this.owners.find(new OwnerId(6));
		assertThat(optionalOwner.isDefined()).isTrue();
		owner6 = optionalOwner.get();
		pet7 = owner6.getPet(7);
		assertThat(pet7.getName()).isEqualTo(newName);
	}

	@Test
	void shouldFindVets() {
		Vector<Vet> vets = this.vets.findAll();

		Vet vet = EntityUtils.getById(vets, Vet.class, 3, v -> v.getId().id());
		assertThat(vet.getLastName()).isEqualTo("Douglas");
		assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
		assertThat(vet.getSpecialties().get(0).getName()).isEqualTo("dentistry");
		assertThat(vet.getSpecialties().get(1).getName()).isEqualTo("surgery");
	}

	@Test
	@Transactional
	void shouldAddNewVisitForPet() {
		Option<Owner> optionalOwner = this.owners.find(new OwnerId(6));
		assertThat(optionalOwner.isDefined()).isTrue();
		Owner owner6 = optionalOwner.get();

		Pet pet7 = owner6.getPet(7);
		int found = pet7.getVisits().size();
		Visit visit = new Visit();
		visit.setDescription("test");

		owner6.addVisit(pet7.getId().id(), visit);
		this.owners.save(owner6);

		assertThat(pet7.getVisits()) //
			.hasSize(found + 1) //
			.allMatch(value -> value.getId() != null);
	}

	@Test
	void shouldFindVisitsByPetId() {
		Option<Owner> optionalOwner = this.owners.find(new OwnerId(6));
		assertThat(optionalOwner.isDefined()).isTrue();
		Owner owner6 = optionalOwner.get();

		Pet pet7 = owner6.getPet(7);
		Vector<Visit> visits = pet7.getVisits();

		assertThat(visits) //
			.hasSize(2) //
			.element(0)
			.extracting(Visit::getDate)
			.isNotNull();
	}

	@Test
	@Transactional
	void shouldFailToInsertDuplicatePetNameForSameOwner() {
		Option<Owner> optionalOwner = this.owners.find(new OwnerId(1));
		assertThat(optionalOwner.isDefined()).isTrue();
		Owner owner = optionalOwner.get();

		Pet pet1 = new Pet();
		pet1.setName("DuplicatePetName");
		Vector<PetType> types = this.types.findPetTypes();
		pet1.setType(EntityUtils.getById(types, PetType.class, 1, pt -> pt.getId().id()));
		pet1.setBirthDate(LocalDate.now());
		owner.addPet(pet1);
		this.owners.saveAndFlush(owner);

		Pet pet2 = new Pet();
		pet2.setName("duplicatepetname"); // Case-insensitive duplicate name
		pet2.setType(EntityUtils.getById(types, PetType.class, 1, pt -> pt.getId().id()));
		pet2.setBirthDate(LocalDate.now());
		owner.addPet(pet2);

		assertThrows(DataIntegrityViolationException.class, () -> {
			this.owners.saveAndFlush(owner);
		});
	}

	@Test
	@Transactional
	void shouldAllowSamePetNameForDifferentOwners() {
		Vector<PetType> types = this.types.findPetTypes();
		PetType catType = EntityUtils.getById(types, PetType.class, 1, pt -> pt.getId().id());

		Option<Owner> owner1Opt = this.owners.find(new OwnerId(1));
		assertThat(owner1Opt.isDefined()).isTrue();
		Owner owner1 = owner1Opt.get();

		Pet pet1 = new Pet();
		pet1.setName("SamePetName");
		pet1.setType(catType);
		pet1.setBirthDate(LocalDate.now());
		owner1.addPet(pet1);
		this.owners.saveAndFlush(owner1);

		Option<Owner> owner2Opt = this.owners.find(new OwnerId(2));
		assertThat(owner2Opt.isDefined()).isTrue();
		Owner owner2 = owner2Opt.get();

		Pet pet2 = new Pet();
		pet2.setName("samepetname"); // Case-insensitive duplicate name, but for a
										// different owner
		pet2.setType(catType);
		pet2.setBirthDate(LocalDate.now());
		owner2.addPet(pet2);

		// Saving for different owner should succeed
		this.owners.saveAndFlush(owner2);

		// Verify both exist
		assertThat(owner1.getPet("SamePetName")).isNotNull();
		assertThat(owner2.getPet("samepetname")).isNotNull();
	}

}
