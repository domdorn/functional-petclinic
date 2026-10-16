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

import org.hibernate.annotations.Type;
import org.jspecify.annotations.Nullable;
import org.springframework.samples.petclinic.model.NamedEntity;

import dev.zazr.control.Option;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author Juergen Hoeller Can be Cat, Dog, Hamster...
 */
@Entity
@Table(name = "types")
public class PetType extends NamedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	@Type(PetTypeIdType.class)
	private @Nullable PetTypeId id;

	/**
	 * The typed identity of this pet type, or empty when it has not been persisted yet.
	 * @return an {@link Option} of {@link PetTypeId}
	 */
	public Option<PetTypeId> petTypeId() {
		return Option.ofNullable(this.id);
	}

	public @Nullable PetTypeId getId() {
		return this.id;
	}

	public void setId(PetTypeId id) {
		this.id = id;
	}

	public boolean isNew() {
		return this.id == null;
	}

}
