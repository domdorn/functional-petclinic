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
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.jspecify.annotations.Nullable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.model.NamedEntity;

import dev.zazr.collection.Vector;
import dev.zazr.control.Option;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * Simple business object representing a pet.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Wick Dynex
 */
@Entity
@Table(name = "pets")
public class Pet extends NamedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	@Type(PetIdType.class)
	private @Nullable PetId id;

	@Column
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthDate;

	@ManyToOne
	@JoinColumn(name = "type_id")
	private PetType type;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	// SUBSELECT avoids a multi-key SQL-array load keyed by the typed PetId when several
	// pets are loaded, which the PostgreSQL array binder cannot unwrap (see gotchas.md).
	@Fetch(FetchMode.SUBSELECT)
	@JoinColumn(name = "pet_id")
	@OrderBy("date ASC")
	private final Set<Visit> visits = new LinkedHashSet<>();

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public LocalDate getBirthDate() {
		return this.birthDate;
	}

	public PetType getType() {
		return this.type;
	}

	public void setType(PetType type) {
		this.type = type;
	}

	public Vector<Visit> getVisits() {
		return Vector.ofAll(this.visits);
	}

	public void addVisit(Visit visit) {
		this.visits.add(visit);
	}

	/**
	 * The typed identity of this pet, or empty when it has not been persisted yet.
	 * @return an {@link Option} of {@link PetId}
	 */
	public Option<PetId> petId() {
		return Option.ofNullable(this.id);
	}

	public @Nullable PetId getId() {
		return this.id;
	}

	public void setId(PetId id) {
		this.id = id;
	}

	public boolean isNew() {
		return this.id == null;
	}

}
