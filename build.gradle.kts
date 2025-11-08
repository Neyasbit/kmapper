import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.kotlin.dsl.configure

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.gradleMavenPublish) apply false
    alias(libs.plugins.detekt)
}

allprojects {
    afterEvaluate {

        val settingsPath = rootProject.file("settings.gradle.kts").absolutePath
        val projectBuildGradlePath = rootProject.file("build.gradle.kts").absolutePath

        val targetBuildGradlePath = file("build.gradle.kts").absolutePath

        plugins.apply(libs.plugins.detekt.get().pluginId)

        configure<DetektExtension> {
            buildUponDefaultConfig = true
            val rootConfigFile =
                rootProject.rootDir.resolve("code-quality/detekt/detekt.yml")
            val moduleConfigFile = projectDir.resolve("code-quality/detekt/detekt.yml")
            val configFiles = mutableListOf<File>().apply {
                add(rootConfigFile)
                if (moduleConfigFile.exists()) {
                    add(moduleConfigFile)
                }
            }
            config.from(configFiles)
            autoCorrect = true
            parallel = true

            baseline = file("code-quality/baseline.xml")

            source.setFrom(
                // src covers it all, but it works strangely.
                "src/main/java",
                "src/main/kotlin",
                "src/commonMain/kotlin",
                "src/commonTest",
                "src/iosMain",
                "src/jvmMain",
                "src/wasmJsMain",
                "src/androidMain",
                "src/nativeMain",
                "src/jsMain",
                targetBuildGradlePath,
                projectBuildGradlePath,
                settingsPath
            )
        }

        dependencies {
            add("detektPlugins", libs.detekt.formatting)
            add("detektPlugins", libs.detekt.rules.libraries)
            add("detektPlugins", libs.detekt.compose.rules)
        }
    }
}
