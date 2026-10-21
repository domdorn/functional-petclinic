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
package org.springframework.samples.petclinic.vet.web;

import org.springframework.samples.petclinic.vet.VetId;

import org.springframework.core.convert.converter.Converter;

/**
 * Converts a path-variable string such as {@code {vetId}} into a typed {@link VetId}, so
 * controllers can receive the typed identity instead of a raw {@code int}. Wrapping an
 * absent/optional value in {@link dev.zazr.control.Option} is handled generically by the
 * {@code OptionPathVariableArgumentResolver}; this converter only produces the id itself.
 */
public class StringToVetIdConverter implements Converter<String, VetId> {

	@Override
	public VetId convert(String source) {
		return new VetId(Integer.parseInt(source));
	}

}
