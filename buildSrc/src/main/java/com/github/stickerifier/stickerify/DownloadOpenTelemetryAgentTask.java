package com.github.stickerifier.stickerify;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public abstract class DownloadOpenTelemetryAgentTask extends DefaultTask {

	public static final String DEFAULT_TASK_NAME = "downloadOpenTelemetryAgent";

	@Input
	public abstract Property<@NotNull String> getVersion();

	@OutputFile
	public abstract RegularFileProperty getDestinationFile();

	@Inject
	public DownloadOpenTelemetryAgentTask() {
		setGroup("distribution");
		setDescription("Downloads OpenTelemetry agent jar.");
	}

	@TaskAction
	public void downloadJar() throws IOException {
		var version = getVersion().get();
		var urlString = "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v%s/opentelemetry-javaagent.jar".formatted(version);
		var targetFile = getDestinationFile().get().getAsFile();

		var _ = targetFile.getParentFile().mkdirs();
		try (var inputStream = URI.create(urlString).toURL().openStream()) {
			Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

}
