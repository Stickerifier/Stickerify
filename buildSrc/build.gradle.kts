plugins {
    `java-library`
}

repositories {
    gradlePluginPortal()
}

java.toolchain {
    languageVersion = JavaLanguageVersion.of(27)
    vendor = JvmVendorSpec.ADOPTIUM
}
