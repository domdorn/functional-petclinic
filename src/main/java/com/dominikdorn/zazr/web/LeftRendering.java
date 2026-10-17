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

import java.util.Map;

import org.springframework.http.HttpStatusCode;

/**
 * How an {@code Either}'s {@code Left} value should be rendered: the HTTP status, the
 * view name, and the model attributes to expose to that view. Returned by
 * {@link EitherLeftHandler} and applied by {@link EitherReturnValueHandler}.
 *
 * @param status the HTTP status to send
 * @param viewName the view to render
 * @param model the model attributes for the view
 */
public record LeftRendering(HttpStatusCode status, String viewName, Map<String, Object> model) {

	/**
	 * Convenience for a view with a single {@code message} attribute.
	 * @param status the HTTP status
	 * @param viewName the view to render
	 * @param message the message exposed as the {@code message} model attribute
	 * @return the rendering
	 */
	public static LeftRendering view(HttpStatusCode status, String viewName, String message) {
		return new LeftRendering(status, viewName, Map.of("message", message));
	}

}
