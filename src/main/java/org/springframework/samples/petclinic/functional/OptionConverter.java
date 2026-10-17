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

import org.jspecify.annotations.Nullable;

import dev.zazr.control.Option;

import jakarta.persistence.AttributeConverter;

/**
 * Reusable base that teaches JPA to persist an {@code Option<T>} attribute as a nullable
 * {@code T} column: {@code None} maps to {@code NULL} and {@code Some(value)} to the
 * value. The mapping logic is type-independent, so it lives here once.
 *
 * <p>
 * A single generic {@code AttributeConverter<Option<T>, T>} cannot be used directly —
 * Hibernate must resolve the concrete database column type, and a type variable erases to
 * {@code Object}. So each supported element type gets a trivial concrete subclass (e.g.
 * {@code class OptionLocalDateConverter extends OptionConverter<LocalDate>}), which
 * carries resolvable generics and the {@code @Converter} registration.
 *
 * @param <T> the element type stored in the column
 */
public abstract class OptionConverter<T> implements AttributeConverter<Option<T>, T> {

	@Override
	public @Nullable T convertToDatabaseColumn(@Nullable Option<T> attribute) {
		return (attribute == null) ? null : attribute.getOrNull();
	}

	@Override
	public Option<T> convertToEntityAttribute(@Nullable T dbData) {
		return Option.ofNullable(dbData);
	}

}
