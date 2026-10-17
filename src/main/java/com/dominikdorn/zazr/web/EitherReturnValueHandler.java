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

import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.ModelAndView;

import dev.zazr.control.Either;
import dev.zazr.control.Either.Left;
import dev.zazr.control.Either.Right;

/**
 * Lets a Spring WebMVC controller return {@code Either<L, R>} so that failures travel as
 * values rather than exceptions.
 *
 * <p>
 * The {@code Right} value is rendered as the controller would render it directly: a view
 * name ({@link String}) or a {@link ModelAndView}. The {@code Left} value carries no
 * assumption about its type — this handler stays error-model-agnostic and delegates to
 * the application-supplied {@link EitherLeftHandler}, which maps the left value to an
 * HTTP status, a view and a model ({@link LeftRendering}).
 *
 * <p>
 * Register it in a
 * {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurer}, passing the
 * application's {@link EitherLeftHandler}.
 */
public class EitherReturnValueHandler implements HandlerMethodReturnValueHandler {

	private final EitherLeftHandler leftHandler;

	public EitherReturnValueHandler(EitherLeftHandler leftHandler) {
		this.leftHandler = leftHandler;
	}

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
			case Left(var error) -> renderLeft(error, mavContainer);
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

	private void renderLeft(Object left, ModelAndViewContainer mavContainer) {
		LeftRendering rendering = this.leftHandler.render(left);
		mavContainer.setStatus(rendering.status());
		mavContainer.addAllAttributes(rendering.model());
		mavContainer.setViewName(rendering.viewName());
	}

}
