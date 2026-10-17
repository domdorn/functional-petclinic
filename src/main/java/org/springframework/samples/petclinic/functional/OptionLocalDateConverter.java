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

import org.jspecify.annotations.Nullable;

import dev.zazr.control.Option;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Teaches JPA to persist an {@code Option<LocalDate>} field: {@code None} maps to a
 * {@code NULL} column and {@code Some(date)} to the date. Unlike {@code @Id}, regular
 * attributes are allowed to use an {@link AttributeConverter}, so an optional column can
 * be modelled as {@link Option} instead of a nullable {@link LocalDate}.
 *
 * <p>
 * Apply with {@code @Convert(converter = OptionLocalDateConverter.class)} on the field.
 */
@Converter
public class OptionLocalDateConverter implements AttributeConverter<Option<LocalDate>, LocalDate> {

	@Override
	public @Nullable LocalDate convertToDatabaseColumn(@Nullable Option<LocalDate> attribute) {
		return (attribute == null) ? null : attribute.getOrNull();
	}

	@Override
	public Option<LocalDate> convertToEntityAttribute(@Nullable LocalDate dbData) {
		return Option.ofNullable(dbData);
	}

}
