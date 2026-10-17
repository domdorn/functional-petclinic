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

import java.time.LocalDate;

import jakarta.persistence.Converter;

/**
 * Maps an {@code Option<LocalDate>} attribute to a nullable {@code DATE} column. The
 * mapping logic lives in {@link OptionConverter}; this subclass only binds the element
 * type and registers the converter.
 *
 * <p>
 * {@code autoApply = true} means every {@code Option<LocalDate>} attribute in the
 * persistence unit uses it automatically — no {@code @Convert} on the field — as long as
 * this converter is visible to the persistence unit (here it lives under the
 * application's scanned packages; a reusable library would register it via a Hibernate
 * {@code MetadataBuilderContributor} or a Spring Boot auto-configuration instead).
 */
@Converter(autoApply = true)
public class OptionLocalDateConverter extends OptionConverter<LocalDate> {

}
