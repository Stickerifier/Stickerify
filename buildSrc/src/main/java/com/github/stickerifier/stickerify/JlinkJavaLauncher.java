package com.github.stickerifier.stickerify;

import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.jvm.toolchain.JavaInstallationMetadata;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.jetbrains.annotations.NotNull;

public record JlinkJavaLauncher(Provider<JavaInstallationMetadata> metadata, Provider<RegularFile> executablePath) implements JavaLauncher {
	@Override
	public @NotNull JavaInstallationMetadata getMetadata() {
		return metadata.get();
	}

	@Override
	public @NotNull RegularFile getExecutablePath() {
		return executablePath.get();
	}
}
