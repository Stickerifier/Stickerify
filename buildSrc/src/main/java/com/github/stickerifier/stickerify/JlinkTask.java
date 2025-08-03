package com.github.stickerifier.stickerify;

import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class JlinkTask extends DefaultTask {

	@Input
	public abstract ListProperty<String> getOptions();

	@Input
	public abstract ListProperty<String> getModules();

	@OutputDirectory
	public abstract DirectoryProperty getJreDir();

	@Nested
	protected abstract Property<JavaLauncher> getJavaLauncher();

	@Inject
	protected abstract JavaToolchainService getJavaToolchainService();

	@Inject
	protected abstract FileSystemOperations getFs();

	@Inject
	protected abstract ExecOperations getExec();

	public JlinkTask() {
		getOptions().convention(List.of());
		getModules().convention(List.of("ALL-MODULE-PATH"));
		getJreDir().convention(getProject().getLayout().getBuildDirectory().dir("jre"));

		var toolchain = getProject().getExtensions().getByType(JavaPluginExtension.class).getToolchain();
		getJavaLauncher().convention(getJavaToolchainService().launcherFor(toolchain));
	}

	@TaskAction
	public void performAction() {
		var installationPath = getJavaLauncher().get().getMetadata().getInstallationPath();

		var jlink = installationPath.file(Os.isFamily(Os.FAMILY_WINDOWS) ? "bin\\jlink.exe" : "bin/jlink");
		var jmods = installationPath.dir("jmods");

		getFs().delete(deleteSpec -> deleteSpec.delete(getJreDir()));

		var stdout = new ByteArrayOutputStream();
		var stderr = new ByteArrayOutputStream();

		var result = getExec().exec(execSpec -> {
			execSpec.setIgnoreExitValue(true);

			var commandLine = new ArrayList<String>();
			commandLine.add(jlink.toString());
			commandLine.add("-v");
			commandLine.addAll(getOptions().get());
			commandLine.add("--module-path");
			commandLine.add(jmods.toString());
			commandLine.add("--add-modules");
			commandLine.add(String.join(",", getModules().get()));
			commandLine.add("--output");
			commandLine.add(getJreDir().get().toString());

			execSpec.setCommandLine(commandLine);

			execSpec.setStandardOutput(stdout);
			execSpec.setErrorOutput(stderr);
		});

		var stdoutStr = stdout.toString();
		var stderrStr = stderr.toString();

		if (!stdoutStr.isEmpty()) {
			getLogger().info(stdoutStr);
		}

		if (result.getExitValue() != 0 && !stderrStr.isEmpty()) {
			getLogger().log(LogLevel.ERROR, stderrStr);
		}

		result.assertNormalExitValue();
	}

}
