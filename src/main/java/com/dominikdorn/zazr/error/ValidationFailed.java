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
package com.dominikdorn.zazr.error;

import dev.zazr.collection.NonEmptyVector;

/**
 * Category of {@link DomainError} for input that failed validation, carrying every
 * {@link Violation} (not just the first). Rendered as HTTP 400 by
 * {@link EitherReturnValueHandler}; for server-rendered forms the violations are instead
 * bridged into Spring's {@code BindingResult} by {@link Validations}.
 */
public record ValidationFailed(NonEmptyVector<Violation> violations) implements DomainError {

	@Override
	public String message() {
		StringBuilder message = new StringBuilder();
		for (Violation violation : this.violations) {
			if (!message.isEmpty()) {
				message.append("; ");
			}
			message.append(violation.message());
		}
		return message.toString();
	}

}
