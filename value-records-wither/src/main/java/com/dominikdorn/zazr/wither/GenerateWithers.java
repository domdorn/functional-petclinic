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
package com.dominikdorn.zazr.wither;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code record} (including a {@code value record}) for which fluent {@code withX}
 * copy methods should be generated.
 *
 * <p>
 * The processor generates a sibling interface {@code <Record>With} in the same package. The
 * record opts in by implementing it:
 *
 * <pre>{@code
 * @GenerateWithers
 * public value record Owner(OwnerId id, String name) implements OwnerWith { }
 *
 * Owner renamed = owner.withName("Dibbert"); // generated, returns a new Owner
 * }</pre>
 *
 * The {@code implements OwnerWith} is required because an annotation processor can only
 * create new files, never modify the annotated record itself. The forward reference to the
 * not-yet-generated interface is resolved across annotation-processing rounds.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateWithers {

}
