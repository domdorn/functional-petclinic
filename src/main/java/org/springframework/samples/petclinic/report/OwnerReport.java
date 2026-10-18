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

import org.springframework.samples.petclinic.functional.Violation;

import dev.zazr.collection.Vector;

/**
 * A per-owner care report returned as JSON: a line per pet plus a strip of data-quality
 * warnings accumulated across all of the owner's pets.
 *
 * @param owner the owner's full name
 * @param pets one {@link PetSummary} per pet
 * @param warnings every data-quality violation found, accumulated (not short-circuited)
 */
public value record OwnerReport(String owner, Vector<PetSummary> pets, Vector<Violation> warnings) {
}
