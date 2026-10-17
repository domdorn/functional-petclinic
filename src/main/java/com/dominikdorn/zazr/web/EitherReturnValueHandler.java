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

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import dev.zazr.control.Either;
import dev.zazr.control.Either.Left;
import dev.zazr.control.Either.Right;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Lets a Spring WebMVC controller return {@code Either<L, R>} so that failures travel as
 * values rather than exceptions. Works for both view-rendering and {@code @ResponseBody}
 * / {@code @RestController} (JSON) endpoints.
 *
 * <ul>
 * <li><b>Body endpoints</b> — the {@code Right} value is written with the message
 * converters (e.g. JSON); the {@code Left} is mapped by the {@link EitherLeftHandler} to
 * a status and a model, which is written as the body.</li>
 * <li><b>View endpoints</b> — the {@code Right} value is a view name ({@link String}) or
 * a {@link ModelAndView}; the {@code Left} becomes the status + view + model from the
 * {@link EitherLeftHandler}.</li>
 * </ul>
 *
 * <p>
 * It must be registered <em>ahead</em> of the built-in handlers so it claims
 * {@code @ResponseBody Either} returns before {@code RequestResponseBodyMethodProcessor}
 * would serialize the raw {@code Either}.
 */
public class EitherReturnValueHandler implements HandlerMethodReturnValueHandler {

	private final EitherLeftHandler leftHandler;

	private final RequestResponseBodyMethodProcessor bodyProcessor;

	public EitherReturnValueHandler(List<HttpMessageConverter<?>> messageConverters, EitherLeftHandler leftHandler) {
		this.leftHandler = leftHandler;
		this.bodyProcessor = new RequestResponseBodyMethodProcessor(messageConverters);
	}

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return Either.class.isAssignableFrom(returnType.getParameterType());
	}

	@Override
	public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
		if (returnValue == null) {
			// A null Either is a programming error, but tolerate it as "nothing to
			// render".
			mavContainer.setRequestHandled(true);
			return;
		}
		Either<?, ?> either = (Either<?, ?>) returnValue;
		if (writesBody(returnType)) {
			handleBody(either, returnType, mavContainer, webRequest);
		}
		else {
			handleView(either, mavContainer);
		}
	}

	private boolean writesBody(MethodParameter returnType) {
		return returnType.hasMethodAnnotation(ResponseBody.class)
				|| AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), ResponseBody.class);
	}

	private void handleBody(Either<?, ?> either, MethodParameter returnType, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest) throws Exception {
		// The Right generic of Either<L, R>; the body is serialized against this type.
		MethodParameter bodyType = returnType.nested();
		switch (either) {
			case Right(var value) -> this.bodyProcessor.handleReturnValue(value, bodyType, mavContainer, webRequest);
			case Left(var error) -> {
				LeftRendering rendering = this.leftHandler.render(error);
				HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
				if (response != null) {
					response.setStatus(rendering.status().value());
				}
				this.bodyProcessor.handleReturnValue(rendering.model(), bodyType, mavContainer, webRequest);
			}
		}
	}

	private void handleView(Either<?, ?> either, ModelAndViewContainer mavContainer) {
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
