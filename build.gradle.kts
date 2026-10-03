import com.github.stickerifier.stickerify.JlinkJavaLauncher
import com.github.stickerifier.stickerify.JlinkTask
import com.github.stickerifier.stickerify.JunitSeedArgumentProvider
import io.spring.gradle.nullability.NullabilityOptions
import org.gradle.internal.buildconfiguration.DaemonJvmPropertiesConfigurator

plugins {
    java
    application
    alias(libs.plugins.spring.nullability)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.gson)
    implementation(libs.jspecify)
    implementation(libs.logback.classic)
    implementation(libs.logstash.logback.encoder)
    implementation(libs.opentelemetry.instrumentation.annotations)
    implementation(libs.telegram.bot.api)
    implementation(libs.tika)

    constraints {
        add("implementation", libs.jackson.core)
    }

    testImplementation(libs.hamcrest)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockwebserver)
    testRuntimeOnly(libs.junit.platform)
}

group = "com.github.stickerifier"
version = "2.0"
description = "Telegram bot to convert medias into the format required to be used as Telegram stickers"

java.toolchain {
    languageVersion = JavaLanguageVersion.of(27)
    vendor = JvmVendorSpec.ADOPTIUM
}

tasks.named<UpdateDaemonJvm>(DaemonJvmPropertiesConfigurator.TASK_NAME) {
    languageVersion = JavaLanguageVersion.of(27)
    vendor = JvmVendorSpec.ADOPTIUM
}

val jlink = tasks.register<JlinkTask>(JlinkTask.DEFAULT_TASK_NAME) {
    description = "Generates a minimal JRE for the project with compact object headers archive."

    options = listOf("--strip-debug", "--no-header-files", "--no-man-pages", "--ignore-modified-runtime", "--generate-cds-archive")
    modules = listOf(
            "java.instrument", // for JUnit
            "java.naming",     // for Logback
            "java.management", // for OpenTelemetry
            "java.sql",        // for Tika
            "jdk.unsupported"  // for Gson
    )
    includeModulePath = false
    javaCompiler = javaToolchains.compilerFor(java.toolchain)
}

nullability {
    jspecify {
        experimental = true
    }
}

val CompileOptions.nullability: NullabilityOptions
    get() = (this as ExtensionAware).extensions.getByName("nullability") as NullabilityOptions

tasks.named<JavaCompile>(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME) {
    options.nullability.checking = "tests"
    options.nullability.jspecify {
        experimental = true
    }
}

tasks.test {
    inputs.dir(jlink.map { it.outputDirectory.get().asFile })
    javaLauncher = providers.provider { JlinkJavaLauncher(jlink.get()) }

    useJUnitPlatform()
    jvmArgs("--enable-final-field-mutation=ALL-UNNAMED")

    testLogging {
        events("started", "passed", "failed", "skipped")
    }

    val seedProvider = JunitSeedArgumentProvider(providers.gradleProperty("junitSeed"))
    jvmArgumentProviders.add(seedProvider)

    doFirst {
        println("Test seed: ${seedProvider.resolveSeed()}")
    }
}

application {
    mainClass = "com.github.stickerifier.stickerify.runner.Main"
    applicationDefaultJvmArgs = listOf("--enable-final-field-mutation=ALL-UNNAMED")
}

distributions {
    main {
        contents {
            from(jlink)
        }
    }
}

tasks.named<CreateStartScripts>(ApplicationPlugin.TASK_START_SCRIPTS_NAME) {
    (unixStartScriptGenerator as TemplateBasedScriptGenerator).template = resources.text.fromFile("src/main/resources/customUnixStartScript.txt")
    (windowsStartScriptGenerator as TemplateBasedScriptGenerator).template = resources.text.fromFile("src/main/resources/customWindowsStartScript.txt")
}
