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
package org.springframework.samples.petclinic.functional.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.samples.petclinic.functional.DomainError;
import org.springframework.samples.petclinic.functional.NotFound;
import org.springframework.samples.petclinic.functional.ValidationFailed;

import com.dominikdorn.zazr.web.EitherLeftHandler;
import com.dominikdorn.zazr.web.LeftRendering;

/**
 * Maps this application's {@link DomainError} taxonomy to an HTTP rendering for the
 * generic {@code EitherReturnValueHandler}: {@link NotFound} to 404,
 * {@link ValidationFailed} to 400, anything else to 500. View endpoints render the shared
 * {@code error} view with the error's message; {@code @ResponseBody}/JSON endpoints get
 * an RFC-7807 {@link ProblemDetail} body carrying the same status and message.
 */
public class DomainErrorLeftHandler implements EitherLeftHandler {

	private static final String ERROR_VIEW = "error";

	@Override
	public LeftRendering render(Object left) {
		DomainError error = (DomainError) left;
		HttpStatus status = switch (error) {
			case NotFound ignored -> HttpStatus.NOT_FOUND;
			case ValidationFailed ignored -> HttpStatus.BAD_REQUEST;
			default -> HttpStatus.INTERNAL_SERVER_ERROR;
		};
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, error.message());
		return LeftRendering.view(status, ERROR_VIEW, error.message()).withBody(problem);
	}

}
