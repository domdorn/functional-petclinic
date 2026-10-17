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

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatusCode;

/**
 * How an {@code Either}'s {@code Left} value should be rendered. It carries enough for
 * both kinds of endpoint, so the same value serves either:
 * <ul>
 * <li><b>View endpoints</b> use {@link #status()}, {@link #viewName()} and
 * {@link #model()}.</li>
 * <li><b>Body ({@code @ResponseBody}/JSON) endpoints</b> use {@link #status()} and
 * {@link #body()} — e.g. a {@link org.springframework.http.ProblemDetail}. When
 * {@code body} is {@code null} the body endpoint falls back to writing
 * {@link #model()}.</li>
 * </ul>
 * Returned by {@link EitherLeftHandler} and applied by {@link EitherReturnValueHandler}.
 *
 * @param status the HTTP status to send
 * @param viewName the view to render (view endpoints)
 * @param model the model attributes for the view
 * @param body the object to serialize for body endpoints, or {@code null} to fall back to
 * the model
 */
public record LeftRendering(HttpStatusCode status, String viewName, Map<String, Object> model, @Nullable Object body) {

	/**
	 * Convenience for a view with a single {@code message} attribute and no dedicated
	 * body object (body endpoints fall back to the model).
	 * @param status the HTTP status
	 * @param viewName the view to render
	 * @param message the message exposed as the {@code message} model attribute
	 * @return the rendering
	 */
	public static LeftRendering view(HttpStatusCode status, String viewName, String message) {
		return new LeftRendering(status, viewName, Map.of("message", message), null);
	}

	/**
	 * Returns a copy of this rendering with the given body object used for body
	 * endpoints.
	 * @param body the object to serialize for {@code @ResponseBody}/JSON endpoints
	 * @return a rendering with the body attached
	 */
	public LeftRendering withBody(Object body) {
		return new LeftRendering(this.status, this.viewName, this.model, body);
	}

}
