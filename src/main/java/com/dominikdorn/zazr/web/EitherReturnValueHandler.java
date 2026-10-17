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
package com.dominikdorn.zazr.web;

import com.dominikdorn.zazr.error.DomainError;
import com.dominikdorn.zazr.error.NotFound;
import com.dominikdorn.zazr.error.ValidationFailed;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.ModelAndView;

import dev.zazr.control.Either;
import dev.zazr.control.Either.Left;
import dev.zazr.control.Either.Right;

/**
 * Lets a Spring MVC controller return {@code Either<DomainError, V>} so that failures
 * travel as values rather than exceptions.
 *
 * <p>
 * The {@code Right} value is rendered as the controller would render it directly: a view
 * name ({@link String}) or a {@link ModelAndView}. The {@code Left} value is a
 * {@link DomainError}; its category (e.g. {@link NotFound}) selects an HTTP status, and
 * it is shown through the shared {@code error} view with its
 * {@link DomainError#message()} in the model.
 *
 * <p>
 * Register it in a
 * {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurer}.
 */
public class EitherReturnValueHandler implements HandlerMethodReturnValueHandler {

	private static final String ERROR_VIEW = "error";

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return Either.class.isAssignableFrom(returnType.getParameterType());
	}

	@Override
	public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest) {
		if (returnValue == null) {
			// A null Either is a programming error, but tolerate it as "nothing to
			// render".
			mavContainer.setRequestHandled(true);
			return;
		}
		Either<?, ?> either = (Either<?, ?>) returnValue;
		switch (either) {
			case Right(var value) -> renderSuccess(value, mavContainer);
			case Left(var error) -> renderError((DomainError) error, mavContainer);
		}
	}

	private void renderSuccess(Object value, ModelAndViewContainer mavContainer) {
		switch (value) {
			case ModelAndView mav -> {
				if (mav.getViewName() != null) {
					mavContainer.setViewName(mav.getViewName());
				}
				else if (mav.getView() != null) {
					mavContainer.setView(mav.getView());
				}
				mavContainer.addAllAttributes(mav.getModel());
				if (mav.getStatus() != null) {
					mavContainer.setStatus(mav.getStatus());
				}
			}
			case String viewName -> mavContainer.setViewName(viewName);
			default -> throw new IllegalStateException("Unsupported Either Right value: " + value.getClass().getName()
					+ ". Return a view name (String) or a ModelAndView.");
		}
	}

	private void renderError(DomainError error, ModelAndViewContainer mavContainer) {
		HttpStatus status = switch (error) {
			case NotFound ignored -> HttpStatus.NOT_FOUND;
			case ValidationFailed ignored -> HttpStatus.BAD_REQUEST;
			default -> HttpStatus.INTERNAL_SERVER_ERROR;
		};
		mavContainer.setStatus(status);
		mavContainer.addAttribute("message", error.message());
		mavContainer.setViewName(ERROR_VIEW);
	}

}
