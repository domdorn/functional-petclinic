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
package org.springframework.samples.petclinic.vet;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import dev.zazr.collection.Vector;

/**
 * Simple domain object representing a list of veterinarians. Mostly here to be used for
 * the 'vets' {@link org.springframework.web.servlet.view.xml.MarshallingView}. Holds an
 * immutable zazr {@link Vector}; zazr-jackson renders it as a JSON array.
 *
 * @author Arjen Poutsma
 */
@XmlRootElement
public class Vets {

	private Vector<Vet> vets = Vector.empty();

	@XmlElement
	public Vector<Vet> getVetList() {
		return this.vets;
	}

	public void setVetList(Vector<Vet> vets) {
		this.vets = vets;
	}

}
