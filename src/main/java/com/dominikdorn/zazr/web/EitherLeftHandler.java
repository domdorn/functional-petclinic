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

/**
 * Application-supplied strategy that {@link EitherReturnValueHandler} delegates to when a
 * controller returns an {@code Either} whose {@code Left} needs rendering. This keeps the
 * handler free of any particular error taxonomy: the application decides how its own
 * error values map to an HTTP response.
 */
@FunctionalInterface
public interface EitherLeftHandler {

	/**
	 * Map a {@code Left} value to the HTTP status, view and model to render.
	 * @param left the left value produced by the controller
	 * @return how to render it
	 */
	LeftRendering render(Object left);

}
