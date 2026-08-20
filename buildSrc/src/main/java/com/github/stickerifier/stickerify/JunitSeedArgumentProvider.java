package com.github.stickerifier.stickerify;

import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.process.CommandLineArgumentProvider;

import java.util.Collections;

public class JunitSeedArgumentProvider implements CommandLineArgumentProvider {

	private final Provider<String> explicitSeed;
	private String resolvedSeed;

	public JunitSeedArgumentProvider(Provider<String> explicitSeed) {
		this.explicitSeed = explicitSeed;
	}

	@Input
	@Optional
	public Provider<String> getExplicitSeed() {
		return explicitSeed;
	}

	public synchronized String resolveSeed() {
		if (resolvedSeed == null) {
			resolvedSeed = explicitSeed.getOrElse(String.valueOf(System.nanoTime()));
		}
		return resolvedSeed;
	}

	@Override
	public Iterable<String> asArguments() {
		return Collections.singletonList("-Djunit.jupiter.execution.order.random.seed=" + resolveSeed());
	}

}
