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

/**
 * A business failure carried as a value on the {@code Left} side of an
 * {@link dev.zazr.control.Either} returned by a controller or service, instead of being
 * thrown as an exception.
 *
 * <p>
 * Concrete failures are {@code value record}s living next to the feature they belong to
 * (for example {@code OwnerNotFound}). They implement a category interface such as
 * {@link NotFound} so the Spring glue can map each category to an HTTP status and view
 * without knowing the individual feature types.
 */
public interface DomainError {

	/**
	 * A human-readable description of the failure, suitable for showing to the user.
	 * @return the message
	 */
	String message();

}
