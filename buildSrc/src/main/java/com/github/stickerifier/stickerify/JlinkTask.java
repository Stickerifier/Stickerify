package com.github.stickerifier.stickerify;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.toolchain.JavaCompiler;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.process.ExecOperations;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class JlinkTask extends DefaultTask {

	@Input
	public abstract ListProperty<@NotNull String> getOptions();

	@Input
	public abstract ListProperty<@NotNull String> getModules();

	/**
	 * If the selected JVM is <a href="https://openjdk.org/jeps/493">JEP 493</a> compatible, then this should be set to false.
	 */
	@Input
	public abstract Property<@NotNull Boolean> getIncludeModulePath();

	@OutputDirectory
	public abstract DirectoryProperty getOutputDirectory();

	@Nested
	protected abstract Property<@NotNull JavaCompiler> getJavaCompiler();

	@Inject
	protected abstract FileSystemOperations getFs();

	@Inject
	protected abstract ExecOperations getExec();

	@Inject
	public JlinkTask(ProjectLayout layout, JavaToolchainService javaToolchain) {
		getOptions().convention(List.of());
		getModules().convention(List.of("ALL-MODULE-PATH"));
		getIncludeModulePath().convention(true);
		getOutputDirectory().convention(layout.getBuildDirectory().dir(getName()));

		var toolchain = getProject().getExtensions().getByType(JavaPluginExtension.class).getToolchain();
		getJavaCompiler().convention(javaToolchain.compilerFor(toolchain));
	}

	@TaskAction
	public void createJre() {
		var installationPath = getJavaCompiler().get().getMetadata().getInstallationPath();

		var jlink = installationPath.file("bin/jlink");
		var jmods = installationPath.dir("jmods");

		var jlinkOutput = getOutputDirectory().dir("jre").get().getAsFile();
		getFs().delete(deleteSpec -> deleteSpec.delete(jlinkOutput));

		var stdout = new ByteArrayOutputStream();
		var stderr = new ByteArrayOutputStream();

		var result = getExec().exec(execSpec -> {
			execSpec.setIgnoreExitValue(true);

			var commandLine = new ArrayList<String>();
			commandLine.add(jlink.toString());
			commandLine.addAll(getOptions().get());
			if (getIncludeModulePath().get()) {
				commandLine.add("--module-path");
				commandLine.add(jmods.toString());
			}
			commandLine.add("--add-modules");
			commandLine.add(String.join(",", getModules().get()));
			commandLine.add("--output");
			commandLine.add(jlinkOutput.toString());

			execSpec.setCommandLine(commandLine);

			execSpec.setStandardOutput(stdout);
			execSpec.setErrorOutput(stderr);
		});

		var stdoutStr = stdout.toString();
		var stderrStr = stderr.toString();

		if (!stdoutStr.isEmpty()) {
			getLogger().info(stdoutStr);
		}

		if (result.getExitValue() != 0) {
			if (stderrStr.isEmpty()) {
				getLogger().log(LogLevel.ERROR, "jlink failed with exit code: {}", result.getExitValue());
			} else {
				getLogger().log(LogLevel.ERROR, stderrStr);
			}
		}

		result.assertNormalExitValue();
	}

}
