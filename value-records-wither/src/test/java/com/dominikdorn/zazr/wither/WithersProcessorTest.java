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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Compiles a sample {@code value record} through {@link WithersProcessor} in-process and
 * asserts both that the {@code SampleWith} interface is generated and that real call sites
 * ({@code withId(...).withName(...)}) type-check against it — i.e. the generated copy
 * methods have the right names, parameter types and return type.
 */
class WithersProcessorTest {

	private static final String SAMPLE = """
			package com.example;
			import com.dominikdorn.zazr.wither.GenerateWithers;
			@GenerateWithers
			public value record Sample(int id, String name) implements SampleWith { }
			""";

	// If the generated withers are wrong (missing, bad types, wrong return), this won't
	// compile — so a successful build is the real assertion.
	private static final String USAGE = """
			package com.example;
			public final class Usage {
				static Sample rename(Sample s) {
					return s.withId(5).withName("Dibbert");
				}
			}
			""";

	@Test
	void generatesFluentWithersThatTypeCheck(@TempDir Path tmp) throws IOException {
		Path srcDir = Files.createDirectories(tmp.resolve("src/com/example"));
		Path sample = Files.writeString(srcDir.resolve("Sample.java"), SAMPLE);
		Path usage = Files.writeString(srcDir.resolve("Usage.java"), USAGE);
		Path classes = Files.createDirectories(tmp.resolve("classes"));
		Path generated = Files.createDirectories(tmp.resolve("generated"));

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		boolean success;
		try (StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, null,
				StandardCharsets.UTF_8)) {
			Iterable<? extends JavaFileObject> units = files
				.getJavaFileObjectsFromFiles(List.of(sample.toFile(), usage.toFile()));
			List<String> options = List.of("--release", "28", "--enable-preview", "-d", classes.toString(), "-s",
					generated.toString());
			JavaCompiler.CompilationTask task = compiler.getTask(null, files, diagnostics, options, null, units);
			task.setProcessors(List.of(new WithersProcessor()));
			success = task.call();
		}

		String errors = diagnostics.getDiagnostics().toString();
		assertTrue(success, "compilation (incl. Usage call sites) should succeed:\n" + errors);

		Path withFile = generated.resolve("com/example/SampleWith.java");
		assertTrue(Files.exists(withFile), "SampleWith.java should be generated");
		String code = Files.readString(withFile);
		assertTrue(code.contains("default Sample withId(int id)"), () -> "missing withId:\n" + code);
		assertTrue(code.contains("default Sample withName(java.lang.String name)"),
				() -> "missing withName:\n" + code);
		// withId changes id, copies name via accessor; withName does the reverse.
		assertTrue(code.contains("return new Sample(id, name())"), () -> "withId body wrong:\n" + code);
		assertTrue(code.contains("return new Sample(id(), name)"), () -> "withName body wrong:\n" + code);
		assertFalse(code.contains("value record"), "the generated type is an interface, not a record");
	}

}
