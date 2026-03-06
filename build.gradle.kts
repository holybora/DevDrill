plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.detekt) apply false
}

tasks.register<Copy>("installGitHooks") {
    description = "Installs git pre-commit hook for Detekt and SwiftLint"
    group = "git hooks"

    from("config/git-hooks/pre-commit")
    into(
        providers.exec {
            commandLine("git", "rev-parse", "--git-dir")
        }.standardOutput.asText.map { gitDir ->
            file("${gitDir.trim()}/hooks")
        },
    )

    filePermissions {
        unix("rwxr-xr-x")
    }
}