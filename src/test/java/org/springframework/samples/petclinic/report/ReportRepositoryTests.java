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

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import dev.zazr.collection.Vector;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the JPQL constructor expression in {@link ReportRepository} actually hydrates
 * {@link VisitFact}s from the seed data (joining each visit to its pet's type), against a
 * real database rather than hand-built facts.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ReportRepositoryTests {

	@Autowired
	private ReportRepository repository;

	@Test
	void visitFactsAreHydratedWithPetTypeFromTheJoin() {
		Vector<VisitFact> facts = this.repository.visitFacts();

		// 35 seeded visits, every one carrying a non-blank pet type from the join.
		assertThat(facts.size()).isEqualTo(35);
		assertThat(facts.asJava()).allSatisfy(fact -> {
			assertThat(fact.date()).isNotNull();
			assertThat(fact.petType()).isNotBlank();
		});

		// Pet 7 (Samantha) is a cat and has a March and an August visit.
		assertThat(facts.asJava()).contains(new VisitFact(LocalDate.parse("2026-03-05"), "cat"),
				new VisitFact(LocalDate.parse("2026-08-20"), "cat"));
	}

}
