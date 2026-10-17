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
package org.springframework.samples.petclinic.functional;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerId;
import org.springframework.samples.petclinic.owner.PetId;
import org.springframework.samples.petclinic.owner.PetTypeId;
import org.springframework.samples.petclinic.owner.VisitId;
import org.springframework.samples.petclinic.vet.Specialty;
import org.springframework.samples.petclinic.vet.SpecialtyId;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetId;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Verifies that a typed id serializes as its flat scalar (e.g. {@code 5}) rather than a
 * nested object ({@code {"id":5}}), thanks to {@code @JsonValue} on each id record.
 * Owners are never exposed as JSON by an endpoint, so this guards the contract directly
 * on the mapper; the {@code /vets} endpoint test covers it through the running web layer.
 */
class TypedIdJsonTests {

	// Entities now carry zazr collections (pets, specialties), so use the same
	// module-discovering mapper Spring Boot builds.
	private final JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();

	@Test
	void everyTypedIdSerializesAsABareInt() {
		assertThat(this.mapper.writeValueAsString(new OwnerId(5))).isEqualTo("5");
		assertThat(this.mapper.writeValueAsString(new PetId(7))).isEqualTo("7");
		assertThat(this.mapper.writeValueAsString(new PetTypeId(3))).isEqualTo("3");
		assertThat(this.mapper.writeValueAsString(new VisitId(9))).isEqualTo("9");
		assertThat(this.mapper.writeValueAsString(new VetId(1))).isEqualTo("1");
		assertThat(this.mapper.writeValueAsString(new SpecialtyId(2))).isEqualTo("2");
	}

	@Test
	void ownerIdFieldIsAFlatNumberNotANestedObject() {
		Owner owner = new Owner();
		owner.setId(new OwnerId(5));
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		owner.setAddress("110 W. Liberty St.");
		owner.setCity("Madison");
		owner.setTelephone("6085551023");

		JsonNode id = this.mapper.readTree(this.mapper.writeValueAsString(owner)).get("id");

		assertThat(id.isIntegralNumber()).isTrue();
		assertThat(id.isObject()).isFalse();
		assertThat(id.intValue()).isEqualTo(5);
	}

	@Test
	void vetIdAndNestedSpecialtyIdAreBothFlat() {
		Vet vet = new Vet();
		vet.setId(new VetId(1));
		vet.setFirstName("James");
		vet.setLastName("Carter");
		Specialty radiology = new Specialty();
		radiology.setId(new SpecialtyId(2));
		radiology.setName("radiology");
		vet.addSpecialty(radiology);

		JsonNode vetNode = this.mapper.readTree(this.mapper.writeValueAsString(vet));

		assertThat(vetNode.get("id").intValue()).isEqualTo(1);
		assertThat(vetNode.get("specialties").get(0).get("id").intValue()).isEqualTo(2);
	}

}
