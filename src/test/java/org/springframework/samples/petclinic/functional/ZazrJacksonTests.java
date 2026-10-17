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

import dev.zazr.collection.Vector;
import dev.zazr.control.Option;

import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.json.JsonMapper;

/**
 * Confirms the {@code zazr-jackson} module makes Jackson render zazr types as idiomatic
 * JSON. The mapper is built the way Spring Boot builds its own — discovering modules via
 * the service loader — so a passing test means the module is picked up automatically by
 * the application's {@code ObjectMapper} too.
 */
class ZazrJacksonTests {

	private final JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();

	@Test
	void moduleIsAutoDiscoveredViaServiceLoader() {
		assertThat(MapperBuilder.findModules())
			.anyMatch(module -> module.getClass().getName().equals("dev.zazr.jackson.ZazrModule"));
	}

	@Test
	void vectorSerializesAsJsonArray() {
		assertThat(this.mapper.writeValueAsString(Vector.of(1, 2, 3))).isEqualTo("[1,2,3]");
	}

	@Test
	void optionSerializesAsValueOrNull() {
		assertThat(this.mapper.writeValueAsString(Option.some("hi"))).isEqualTo("\"hi\"");
		assertThat(this.mapper.writeValueAsString(Option.<String>none())).isEqualTo("null");
	}

}
