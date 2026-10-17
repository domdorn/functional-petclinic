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
package org.springframework.samples.petclinic.report;

import java.time.YearMonth;

/**
 * Number of visits in a single calendar month. An internal step of the trend computation
 * (not exposed over the wire): the series of these is zipped with its own tail to derive
 * month-over-month {@link TrendPoint}s.
 *
 * @param month the calendar month
 * @param visits the number of visits recorded in that month
 */
public value record MonthCount(YearMonth month, long visits) {
}
